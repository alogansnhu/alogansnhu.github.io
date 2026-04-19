from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Dict, Iterable, List


@dataclass(frozen=True)
class RescueProfile:
    key: str
    label: str
    breeds: tuple[str, ...]
    sex_upon_outcome: str
    min_age_weeks: int
    max_age_weeks: int


RESCUE_PROFILES: dict[str, RescueProfile] = {
    "WATER": RescueProfile(
        key="WATER",
        label="Water Rescue",
        breeds=("Labrador Retriever Mix", "Chesapeake Bay Retriever", "Newfoundland"),
        sex_upon_outcome="Intact Female",
        min_age_weeks=26,
        max_age_weeks=156,
    ),
    "MOUNTAIN": RescueProfile(
        key="MOUNTAIN",
        label="Mountain / Wilderness Rescue",
        breeds=(
            "German Shepherd",
            "Alaskan Malamute",
            "Old English Sheepdog",
            "Siberian Husky",
            "Rottweiler",
        ),
        sex_upon_outcome="Intact Male",
        min_age_weeks=26,
        max_age_weeks=156,
    ),
    "DISASTER": RescueProfile(
        key="DISASTER",
        label="Disaster Rescue / Individual Tracking",
        breeds=(
            "Doberman Pinscher",
            "German Shepherd",
            "Golden Retriever",
            "Bloodhound",
            "Rottweiler",
        ),
        sex_upon_outcome="Intact Male",
        min_age_weeks=20,
        max_age_weeks=300,
    ),
}


def list_filter_options() -> List[Dict[str, str]]:
    options = [
        {"label": profile.label, "value": profile.key}
        for profile in RESCUE_PROFILES.values()
    ]
    options.append({"label": "Reset", "value": "RESET"})
    return options


def build_rescue_query(filter_value: str | None) -> Dict[str, Any]:
    if not filter_value or filter_value == "RESET":
        return {}

    profile = RESCUE_PROFILES.get(filter_value)
    if profile is None:
        raise ValueError(f"Unknown rescue filter: {filter_value}")

    return {
        "animal_type": "Dog",
        "sex_upon_outcome": profile.sex_upon_outcome,
        "age_upon_outcome_in_weeks": {
            "$gte": profile.min_age_weeks,
            "$lte": profile.max_age_weeks,
        },
        "$or": _breed_regex_conditions(profile.breeds),
    }


def filter_label(filter_value: str | None) -> str:
    if not filter_value or filter_value == "RESET":
        return "Reset (Unfiltered)"
    profile = RESCUE_PROFILES.get(filter_value)
    if profile is None:
        raise ValueError(f"Unknown rescue filter: {filter_value}")
    return profile.label


def _breed_regex_conditions(breeds: Iterable[str]) -> List[Dict[str, Any]]:
    conditions = []
    for breed in breeds:
        token = "Doberman" if breed.lower().startswith("doberman") else breed
        conditions.append({"breed": {"$regex": token, "$options": "i"}})
    return conditions
