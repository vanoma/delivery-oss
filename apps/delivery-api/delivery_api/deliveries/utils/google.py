from decimal import Decimal
import googlemaps  # type: ignore
from typing import Dict, Union
from datetime import datetime, timedelta
from delivery_api.settings import GOOGLE_MAPS_API_KEY

gmaps = googlemaps.Client(key=GOOGLE_MAPS_API_KEY)


class Coordinates(object):
    def __init__(
        self, latitude: Union[Decimal, str], longitude: Union[Decimal, str]
    ) -> None:
        self.latitude = latitude
        self.longitude = longitude

    def to_dict(self) -> Dict[str, str]:
        return {"latitude": str(self.latitude), "longitude": str(self.longitude)}


def get_estimated_duration(
    origin: Coordinates, destination: Coordinates, depart_by: datetime
) -> int:
    """Return Google's estimated duration from origin to destination in seconds."""

    result = gmaps.distance_matrix(
        [origin.to_dict()],
        [destination.to_dict()],
        mode="driving",
        departure_time=depart_by,
    )
    return result["rows"][0]["elements"][0]["duration_in_traffic"]["value"]


def get_estimated_arrival(
    origin: Coordinates, destination: Coordinates, depart_by: datetime
) -> datetime:
    seconds = get_estimated_duration(origin, destination, depart_by)
    return depart_by + timedelta(seconds=seconds)
