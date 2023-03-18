from typing import Dict, Any, List
from requests import Response
from django.conf import settings
from django.utils.translation import gettext_lazy as _
from vanoma_api_utils.http import client
from vanoma_api_utils.auth.jwt import create_service_access_token
from delivery_api.huey import mini_huey
from djangorestframework_camel_case.util import camelize, underscoreize  # type: ignore


def get_package(package_id: str) -> Dict[str, Any]:
    response = client.get(f"{settings.VANOMA_ORDER_API_URL}/packages/{package_id}")

    if not response.ok:
        raise Exception(_(f"Unable to fetch package from order-api: {response.json()}"))

    return underscoreize(response.json())


def get_packages(filters: Dict[str, Any]) -> List[Dict[str, Any]]:
    query = "&".join(
        map(lambda item: f"{item[0]}={item[1]}", camelize(filters).items())
    )
    response = client.get(f"{settings.VANOMA_ORDER_API_URL}/packages?{query}")

    if not response.ok:
        raise Exception(
            _(f"Unable to fetch packages from order-api: {response.json()}")
        )

    return underscoreize(response.json()["results"])


def get_packages_dict(filters: Dict[str, Any]) -> Dict[str, Dict[str, Any]]:
    return {p["package_id"]: p for p in get_packages(filters)}


def update_package(package_id: str, data: Dict[str, Any]) -> Response:
    response = client.patch(
        f"{settings.VANOMA_ORDER_API_URL}/packages/{package_id}",
        data=camelize(data),
        headers={
            "Content-Type": "application/json",
            "Authorization": f'Bearer {create_service_access_token("driver-api")}',
        },
    )

    if not response.ok:
        raise Exception(_(f"Unable to update package on order-api: {response.json()}"))

    return response


@mini_huey.task()
def create_package_event(package_id: str, data: Dict[str, Any]) -> Response:
    return client.put(
        f"{settings.VANOMA_ORDER_API_URL}/packages/{package_id}/events",
        data=camelize(data),
    )
