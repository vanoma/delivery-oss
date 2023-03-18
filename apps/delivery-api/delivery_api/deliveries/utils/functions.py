from typing import Any
from math import radians, cos, sin, asin, sqrt
from vanoma_api_utils.misc import resolve_environment


def resolve_upload_to(*args: Any) -> str:
    return resolve_environment()


def haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """
    Implementation taken from https://www.geeksforgeeks.org/program-distance-two-points-earth/
    """

    # converts from degrees to radians.
    lon1 = radians(lon1)
    lon2 = radians(lon2)
    lat1 = radians(lat1)
    lat2 = radians(lat2)

    # Haversine formula
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2

    c = 2 * asin(sqrt(a))

    # Radius of earth in meters
    r = 6371 * 1000

    # calculate the result
    return c * r
