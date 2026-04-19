from __future__ import annotations

from types import SimpleNamespace

from animal_shelter import AnimalShelter
from rescue_queries import build_rescue_query, filter_label, list_filter_options


class FakeCollection:
    def __init__(self) -> None:
        self.inserted = []
        self.last_find = None
        self.last_aggregate = None
        self.last_update = None
        self.last_delete = None

    def insert_one(self, document):
        self.inserted.append(document)
        return SimpleNamespace(inserted_id="abc123")

    def find(self, query, projection=None, sort=None, limit=0):
        self.last_find = {
            "query": query,
            "projection": projection,
            "sort": sort,
            "limit": limit,
        }
        return [
            {
                "name": "Atlas",
                "breed": "Labrador Retriever Mix",
                "location_lat": "30.2",
                "location_long": "-97.7",
            }
        ]

    def update_one(self, query, update_document):
        self.last_update = (query, update_document, False)
        return SimpleNamespace(modified_count=1)

    def update_many(self, query, update_document):
        self.last_update = (query, update_document, True)
        return SimpleNamespace(modified_count=3)

    def delete_one(self, query):
        self.last_delete = (query, False)
        return SimpleNamespace(deleted_count=1)

    def delete_many(self, query):
        self.last_delete = (query, True)
        return SimpleNamespace(deleted_count=2)

    def aggregate(self, pipeline):
        self.last_aggregate = pipeline
        return [{"breed": "Labrador Retriever Mix", "count": 4}]


def run_tests() -> None:
    fake = FakeCollection()
    shelter = AnimalShelter(collection=fake)

    assert shelter.create({"name": "Atlas"}) is True

    records = shelter.read({"animal_type": "Dog"}, sort=[("breed", 1)], limit=25)
    assert len(records) == 1
    assert fake.last_find["query"] == {"animal_type": "Dog"}
    assert fake.last_find["projection"] == {"_id": 0}
    assert fake.last_find["sort"] == [("breed", 1)]
    assert fake.last_find["limit"] == 25

    modified = shelter.update({"animal_type": "Dog"}, {"name": "Updated"})
    assert modified == 1
    assert fake.last_update == (
        {"animal_type": "Dog"},
        {"$set": {"name": "Updated"}},
        False,
    )

    deleted = shelter.delete({"animal_type": "Dog"})
    assert deleted == 1
    assert fake.last_delete == ({"animal_type": "Dog"}, False)

    counts = shelter.breed_counts({"animal_type": "Dog"}, top_n=5)
    assert counts[0]["count"] == 4
    assert fake.last_aggregate[0] == {"$match": {"animal_type": "Dog"}}

    water_query = build_rescue_query("WATER")
    assert water_query["animal_type"] == "Dog"
    assert water_query["sex_upon_outcome"] == "Intact Female"
    assert len(water_query["$or"]) == 3

    labels = [option["value"] for option in list_filter_options()]
    assert labels[-1] == "RESET"
    assert filter_label("MOUNTAIN") == "Mountain / Wilderness Rescue"
    assert filter_label("RESET") == "Reset (Unfiltered)"

    try:
        shelter.read({"$where": "this.name == 'bad'"})
        raise AssertionError("Unsafe operator should have been rejected")
    except ValueError:
        pass

    print("CS340 enhancement smoke test passed")


if __name__ == "__main__":
    run_tests()
