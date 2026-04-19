from __future__ import annotations

import os
import re
from dataclasses import dataclass
from typing import (
    Any,
    Dict,
    Iterable,
    List,
    Mapping,
    MutableMapping,
    Optional,
    Protocol,
    Sequence,
    Tuple,
)

try:
    from pymongo import MongoClient
    from pymongo.collection import Collection
    from pymongo.errors import PyMongoError
except ModuleNotFoundError:  # Allows offline testing without pymongo installed
    MongoClient = None  # type: ignore[assignment]
    Collection = Any  # type: ignore[assignment]

    class PyMongoError(Exception):
        pass


class CollectionProtocol(Protocol):
    def insert_one(self, document: Mapping[str, Any]) -> Any: ...
    def find(
        self,
        query: Mapping[str, Any],
        projection: Optional[Mapping[str, int]] = None,
        sort: Optional[Sequence[Tuple[str, int]]] = None,
        limit: int = 0,
    ) -> Iterable[Mapping[str, Any]]: ...
    def update_one(
        self, query: Mapping[str, Any], update_document: Mapping[str, Any]
    ) -> Any: ...
    def update_many(
        self, query: Mapping[str, Any], update_document: Mapping[str, Any]
    ) -> Any: ...
    def delete_one(self, query: Mapping[str, Any]) -> Any: ...
    def delete_many(self, query: Mapping[str, Any]) -> Any: ...
    def aggregate(
        self, pipeline: Sequence[Mapping[str, Any]]
    ) -> Iterable[Mapping[str, Any]]: ...


@dataclass(frozen=True)
class MongoConfig:
    username: str
    password: str
    host: str = "localhost"
    port: int = 27017
    database_name: str = "aac"
    collection_name: str = "animals"
    auth_source: str = "aac"

    @classmethod
    def from_env(cls) -> "MongoConfig":
        username = os.getenv("AAC_MONGO_USERNAME") or os.getenv("MONGO_USERNAME")
        password = os.getenv("AAC_MONGO_PASSWORD") or os.getenv("MONGO_PASSWORD")
        host = os.getenv("AAC_MONGO_HOST", "localhost")
        port = int(os.getenv("AAC_MONGO_PORT", "27017"))
        database_name = os.getenv("AAC_DATABASE", "aac")
        collection_name = os.getenv("AAC_COLLECTION", "animals")
        auth_source = os.getenv("AAC_AUTH_SOURCE", database_name)

        if not username or not password:
            raise ValueError(
                "MongoDB credentials are missing. Set AAC_MONGO_USERNAME and AAC_MONGO_PASSWORD in the environment."
            )

        return cls(
            username=username,
            password=password,
            host=host,
            port=port,
            database_name=database_name,
            collection_name=collection_name,
            auth_source=auth_source,
        )

    def validate(self) -> None:
        name_pattern = re.compile(r"^[A-Za-z0-9_.-]+$")
        for label, value in {
            "database_name": self.database_name,
            "collection_name": self.collection_name,
            "auth_source": self.auth_source,
        }.items():
            if not value or not name_pattern.match(value):
                raise ValueError(f"Invalid {label}: {value!r}")

        if not self.username or not self.password:
            raise ValueError("Username and password are required.")

        if not (1 <= int(self.port) <= 65535):
            raise ValueError("Port must be between 1 and 65535.")

    def uri(self) -> str:
        self.validate()
        return (
            f"mongodb://{self.username}:{self.password}@{self.host}:{self.port}"
            f"/?authSource={self.auth_source}"
        )


class AnimalShelter:
    """Provide validated CRUD and aggregation operations for the animal database."""

    ALLOWED_QUERY_OPERATORS = {
        "$gte",
        "$lte",
        "$in",
        "$or",
        "$regex",
        "$options",
        "$eq",
        "$ne",
    }
    ALLOWED_UPDATE_OPERATORS = {"$set"}
    DEFAULT_PROJECTION = {"_id": 0}

    def __init__(
        self,
        username: Optional[str] = None,
        password: Optional[str] = None,
        host: str = "localhost",
        port: int = 27017,
        database_name: str = "aac",
        collection_name: str = "animals",
        auth_source: str = "aac",
        *,
        mongo_config: Optional[MongoConfig] = None,
        collection: Optional[CollectionProtocol] = None,
        **kwargs: Any,
    ) -> None:
        if collection is not None:
            self._client = None
            self._db = None
            self._collection = collection
            return

        if mongo_config is None:
            if username is None or password is None:
                mongo_config = MongoConfig.from_env()
            else:
                mongo_config = MongoConfig(
                    username=username,
                    password=password,
                    host=host,
                    port=port,
                    database_name=database_name,
                    collection_name=collection_name,
                    auth_source=auth_source,
                )

        if MongoClient is None:
            raise RuntimeError(
                "pymongo is not installed. Install pymongo before using the live MongoDB connection."
            )

        uri = mongo_config.uri()
        self._client: MongoClient = MongoClient(uri, **kwargs)
        self._db = self._client[mongo_config.database_name]
        self._collection: Collection = self._db[mongo_config.collection_name]

    @property
    def collection(self) -> CollectionProtocol:
        return self._collection

    def create(self, document: Mapping[str, Any]) -> bool:
        if not isinstance(document, Mapping):
            raise TypeError(
                "document must be a mapping representing a MongoDB document."
            )
        if not document:
            raise ValueError("document must not be empty.")

        try:
            result = self._collection.insert_one(dict(document))
            return getattr(result, "inserted_id", None) is not None
        except PyMongoError as exc:
            raise RuntimeError(f"Create operation failed: {exc}") from exc

    def read(
        self,
        query: Optional[Mapping[str, Any]] = None,
        *,
        projection: Optional[Mapping[str, int]] = None,
        sort: Optional[Sequence[Tuple[str, int]]] = None,
        limit: int = 0,
    ) -> List[Dict[str, Any]]:
        sanitized_query = self._sanitize_query(query or {})
        sanitized_projection = self._sanitize_projection(projection)
        sanitized_sort = self._sanitize_sort(sort)

        if limit < 0:
            raise ValueError("limit must be zero or greater.")

        try:
            cursor = self._collection.find(
                sanitized_query,
                projection=sanitized_projection,
                sort=sanitized_sort,
                limit=limit,
            )
            return [dict(doc) for doc in cursor]
        except PyMongoError as exc:
            raise RuntimeError(f"Read operation failed: {exc}") from exc

    def update(
        self,
        query: Mapping[str, Any],
        new_values: Mapping[str, Any],
        multiple: bool = False,
    ) -> int:
        if not isinstance(query, Mapping) or not isinstance(new_values, Mapping):
            raise TypeError("query and new_values must be mapping instances.")
        if not query:
            raise ValueError("query must not be empty for an update operation.")
        if not new_values:
            raise ValueError("new_values must not be empty for an update operation.")

        sanitized_query = self._sanitize_query(query)
        update_document = self._sanitize_update_document({"$set": dict(new_values)})

        try:
            if multiple:
                result = self._collection.update_many(sanitized_query, update_document)
            else:
                result = self._collection.update_one(sanitized_query, update_document)
            return int(getattr(result, "modified_count", 0))
        except PyMongoError as exc:
            raise RuntimeError(f"Update operation failed: {exc}") from exc

    def delete(self, query: Mapping[str, Any], multiple: bool = False) -> int:
        if not isinstance(query, Mapping):
            raise TypeError("query must be a mapping instance.")
        if not query:
            raise ValueError("query must not be empty for a delete operation.")

        sanitized_query = self._sanitize_query(query)
        try:
            if multiple:
                result = self._collection.delete_many(sanitized_query)
            else:
                result = self._collection.delete_one(sanitized_query)
            return int(getattr(result, "deleted_count", 0))
        except PyMongoError as exc:
            raise RuntimeError(f"Delete operation failed: {exc}") from exc

    def aggregate(self, pipeline: Sequence[Mapping[str, Any]]) -> List[Dict[str, Any]]:
        if not pipeline:
            raise ValueError("pipeline must not be empty.")
        if not isinstance(pipeline, Sequence):
            raise TypeError("pipeline must be a sequence of aggregation stages.")

        sanitized_pipeline = [self._sanitize_stage(stage) for stage in pipeline]

        try:
            return [dict(doc) for doc in self._collection.aggregate(sanitized_pipeline)]
        except PyMongoError as exc:
            raise RuntimeError(f"Aggregation failed: {exc}") from exc

    def breed_counts(
        self, query: Optional[Mapping[str, Any]] = None, *, top_n: int = 10
    ) -> List[Dict[str, Any]]:
        if top_n <= 0:
            raise ValueError("top_n must be greater than zero.")

        pipeline = [
            {"$match": self._sanitize_query(query or {})},
            {"$group": {"_id": "$breed", "count": {"$sum": 1}}},
            {"$sort": {"count": -1, "_id": 1}},
            {"$limit": top_n},
            {"$project": {"breed": "$_id", "count": 1, "_id": 0}},
        ]
        return self.aggregate(pipeline)

    def outcome_counts(
        self, query: Optional[Mapping[str, Any]] = None
    ) -> List[Dict[str, Any]]:
        pipeline = [
            {"$match": self._sanitize_query(query or {})},
            {"$group": {"_id": "$outcome_type", "count": {"$sum": 1}}},
            {"$sort": {"count": -1, "_id": 1}},
            {"$project": {"outcome_type": "$_id", "count": 1, "_id": 0}},
        ]
        return self.aggregate(pipeline)

    def geolocation_points(
        self, query: Optional[Mapping[str, Any]] = None, *, limit: int = 250
    ) -> List[Dict[str, Any]]:
        if limit <= 0:
            raise ValueError("limit must be greater than zero.")

        pipeline = [
            {"$match": self._sanitize_query(query or {})},
            {
                "$project": {
                    "name": 1,
                    "breed": 1,
                    "animal_type": 1,
                    "outcome_type": 1,
                    "location_lat": 1,
                    "location_long": 1,
                    "_id": 0,
                }
            },
            {
                "$match": {
                    "location_lat": {"$ne": None},
                    "location_long": {"$ne": None},
                }
            },
            {"$limit": limit},
        ]
        return self.aggregate(pipeline)

    def close(self) -> None:
        if self._client is not None:
            self._client.close()

    def __enter__(self) -> "AnimalShelter":
        return self

    def __exit__(self, exc_type: Any, exc: Any, tb: Any) -> None:
        self.close()

    def _sanitize_query(self, query: Mapping[str, Any]) -> Dict[str, Any]:
        if not isinstance(query, Mapping):
            raise TypeError("query must be a mapping of search criteria.")
        return self._sanitize_mapping(
            dict(query), allowed_operators=self.ALLOWED_QUERY_OPERATORS
        )

    def _sanitize_projection(
        self, projection: Optional[Mapping[str, int]]
    ) -> Optional[Dict[str, int]]:
        if projection is None:
            return dict(self.DEFAULT_PROJECTION)
        if not isinstance(projection, Mapping):
            raise TypeError("projection must be a mapping of field names to 0 or 1.")

        sanitized: Dict[str, int] = {}
        for field, include in projection.items():
            self._validate_field_name(field)
            if include not in (0, 1):
                raise ValueError("projection values must be 0 or 1.")
            sanitized[str(field)] = int(include)
        return sanitized

    def _sanitize_sort(
        self, sort: Optional[Sequence[Tuple[str, int]]]
    ) -> Optional[List[Tuple[str, int]]]:
        if sort is None:
            return None
        sanitized: List[Tuple[str, int]] = []
        for item in sort:
            if not isinstance(item, tuple) or len(item) != 2:
                raise TypeError("sort entries must be (field, direction) tuples.")
            field, direction = item
            self._validate_field_name(field)
            if direction not in (-1, 1):
                raise ValueError("sort direction must be -1 or 1.")
            sanitized.append((str(field), int(direction)))
        return sanitized

    def _sanitize_update_document(
        self, update_document: Mapping[str, Any]
    ) -> Dict[str, Any]:
        if not isinstance(update_document, Mapping):
            raise TypeError("update_document must be a mapping.")

        sanitized: Dict[str, Any] = {}
        for operator, value in update_document.items():
            if operator not in self.ALLOWED_UPDATE_OPERATORS:
                raise ValueError(f"Update operator {operator!r} is not allowed.")
            if not isinstance(value, Mapping):
                raise TypeError(
                    f"Update operator {operator!r} requires a mapping value."
                )
            sanitized[operator] = self._sanitize_mapping(
                dict(value), allow_operators=False
            )
        return sanitized

    def _sanitize_stage(self, stage: Mapping[str, Any]) -> Dict[str, Any]:
        if not isinstance(stage, Mapping) or len(stage) != 1:
            raise TypeError(
                "Each aggregation stage must be a mapping with exactly one operator."
            )

        operator = next(iter(stage))
        if operator not in {"$match", "$group", "$sort", "$limit", "$project"}:
            raise ValueError(f"Aggregation stage {operator!r} is not allowed.")

        value = stage[operator]
        if operator == "$limit":
            if not isinstance(value, int) or value <= 0:
                raise ValueError("$limit requires a positive integer.")
            return {operator: value}

        if not isinstance(value, Mapping):
            raise TypeError(f"Aggregation stage {operator!r} requires a mapping value.")

        if operator == "$match":
            return {operator: self._sanitize_query(value)}
        return {operator: self._sanitize_aggregation_mapping(dict(value))}

    def _sanitize_aggregation_mapping(
        self, value: MutableMapping[str, Any]
    ) -> Dict[str, Any]:
        sanitized: Dict[str, Any] = {}
        for key, item in value.items():
            key_str = str(key)
            if not key_str.startswith("$"):
                self._validate_field_name(key_str)

            if isinstance(item, Mapping):
                sanitized[key_str] = self._sanitize_aggregation_mapping(dict(item))
            elif isinstance(item, list):
                sanitized[key_str] = [
                    self._sanitize_aggregation_mapping(dict(entry))
                    if isinstance(entry, Mapping)
                    else entry
                    for entry in item
                ]
            else:
                sanitized[key_str] = item
        return sanitized

    def _sanitize_mapping(
        self,
        value: MutableMapping[str, Any],
        *,
        allowed_operators: Optional[set[str]] = None,
        allow_operators: bool = True,
    ) -> Dict[str, Any]:
        sanitized: Dict[str, Any] = {}
        for key, item in value.items():
            key_str = str(key)
            if key_str.startswith("$"):
                if not allow_operators:
                    raise ValueError(f"Operators are not allowed here: {key_str!r}")
                if allowed_operators is not None and key_str not in allowed_operators:
                    raise ValueError(f"Operator {key_str!r} is not allowed.")
            else:
                self._validate_field_name(key_str)

            if isinstance(item, Mapping):
                nested_allowed = (
                    allowed_operators
                    if key_str.startswith("$")
                    else self.ALLOWED_QUERY_OPERATORS
                )
                sanitized[key_str] = self._sanitize_mapping(
                    dict(item),
                    allowed_operators=nested_allowed,
                    allow_operators=True,
                )
            elif isinstance(item, list):
                sanitized[key_str] = [
                    self._sanitize_mapping(
                        dict(entry),
                        allowed_operators=allowed_operators,
                        allow_operators=True,
                    )
                    if isinstance(entry, Mapping)
                    else entry
                    for entry in item
                ]
            else:
                sanitized[key_str] = item
        return sanitized

    @staticmethod
    def _validate_field_name(field_name: str) -> None:
        if not field_name:
            raise ValueError("Field names must not be empty.")
        if field_name.startswith("$"):
            return
        if "." in field_name or "\x00" in field_name:
            raise ValueError(f"Unsafe field name: {field_name!r}")

