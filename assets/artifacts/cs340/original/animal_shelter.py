from typing import Any, Dict, List, Optional

from pymongo import MongoClient
from pymongo.collection import Collection
from pymongo.errors import PyMongoError


class AnimalShelter:
    """Provide CRUD operations for the Grazioso Salvare animal database."""

    def __init__(
        self,
        username: str,
        password: str,
        host: str = "localhost",
        port: int = 27017,
        database_name: str = "aac",
        collection_name: str = "animals",
        auth_source: str = "aac",
        **kwargs: Any,
    ) -> None:
        """
        Create a new AnimalShelter instance connected to MongoDB.

        Parameters
        ----------
        username:
            MongoDB username created for this project.
        password:
            Password for the MongoDB user.
        host:
            MongoDB host name or IP address.
        port:
            MongoDB port number.
        database_name:
            Name of the database that stores the animal records.
        collection_name:
            Name of the collection that stores the animal records.
        auth_source:
            Database that stores the user credentials (aac in this scenario).
        **kwargs:
            Extra keyword arguments passed directly to MongoClient,
            such as tls=True or replicaSet="Cluster0".
        """
        if not username or not password:
            raise ValueError(
                "Username and password are required for MongoDB authentication."
            )

        # Build a standard MongoDB connection string.
        uri = (
            f"mongodb://{username}:{password}@{host}:{port}"
            f"/?authSource={auth_source}"
        )

        # Initialize the client and target collection.
        self._client: MongoClient = MongoClient(uri, **kwargs)
        self._db = self._client[database_name]
        self._collection: Collection = self._db[collection_name]

    @property
    def collection(self) -> Collection:
        """
        Expose the underlying collection for advanced use cases.
        """
        return self._collection

    def create(self, document: Dict[str, Any]) -> bool:
        """
        Insert a single document into the collection.

        Parameters
        ----------
        document:
            A dictionary representing the document to insert.

        Returns
        -------
        bool
            True if the insert succeeds, otherwise False.
        """
        if not isinstance(document, dict):
            raise TypeError(
                "document must be a dict representing a MongoDB document."
            )
        if not document:
            raise ValueError("document must not be empty.")

        try:
            result = self._collection.insert_one(document)
            return result.inserted_id is not None
        except PyMongoError as exc:
            # Would normally be logged in a bigger application
            print(f"Create operation failed: {exc}")
            return False

    def read(self, query: Optional[Dict[str, Any]] = None) -> List[Dict[str, Any]]:
        """
        Query for documents in the collection.

        Parameters
        ----------
        query:
            Key/value lookup criteria to pass to collection.find().
            If None is provided, all documents in the collection are returned.

        Returns
        -------
        list[dict]
            A list of matching documents; returns an empty list if nothing matches.
        """
        if query is None:
            query = {}

        if not isinstance(query, dict):
            raise TypeError("query must be a dict of search criteria.")

        try:
            cursor = self._collection.find(query)
            # Convert the cursor to a concrete list so that callers can reuse it easily.
            return [doc for doc in cursor]
        except PyMongoError as exc:
            print(f"Read operation failed: {exc}")
            return []

    def update(
        self,
        query: Dict[str, Any],
        new_values: Dict[str, Any],
        multiple: bool = False,
    ) -> int:
        """
        Update existing document(s) that match the query criteria.

        Parameters
        ----------
        query:
            Key/value lookup pair used to select documents to update.
        new_values:
            Key/value pairs representing the fields to change.
        multiple:
            If True, update all matching documents; otherwise update only one.

        Returns
        -------
        int
            The number of documents modified.
        """
        if not isinstance(query, dict) or not isinstance(new_values, dict):
            raise TypeError("query and new_values must be dict instances.")
        if not query:
            raise ValueError("query must not be empty for an update operation.")
        if not new_values:
            raise ValueError("new_values must not be empty for an update operation.")

        try:
            update_document = {"$set": new_values}
            if multiple:
                result = self._collection.update_many(query, update_document)
            else:
                result = self._collection.update_one(query, update_document)
            return int(result.modified_count)
        except PyMongoError as exc:
            print(f"Update operation failed: {exc}")
            return 0

    def delete(self, query: Dict[str, Any], multiple: bool = False) -> int:
        """
        Remove document(s) that match the query criteria.

        Parameters
        ----------
        query:
            Key/value lookup pair used to select documents to delete.
        multiple:
            If True, delete all matching documents; otherwise delete only one.

        Returns
        -------
        int
            The number of documents removed from the collection.
        """
        if not isinstance(query, dict):
            raise TypeError("query must be a dict instance.")
        if not query:
            raise ValueError("query must not be empty for a delete operation.")

        try:
            if multiple:
                result = self._collection.delete_many(query)
            else:
                result = self._collection.delete_one(query)
            return int(result.deleted_count)
        except PyMongoError as exc:
            print(f"Delete operation failed: {exc}")
            return 0

    def close(self) -> None:
        """
        Close the underlying MongoDB client connection.
        """
        self._client.close()