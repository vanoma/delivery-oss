from typing import Any, Dict, List
from rest_framework.test import APITestCase
from vanoma_api_utils import dates
from vanoma_api_utils.tests import faker, random_phone_number
from vanoma_api_utils.rest_framework import tests
from delivery_api.staff.models import Staff
from delivery_api.staff.utils.constants import STAFF_STATUS


class ModelsMixin:
    def create_staff(self, **kwargs: Any) -> Staff:
        if "first_name" not in kwargs:
            kwargs["first_name"] = faker.first_name()

        if "last_name" not in kwargs:
            kwargs["last_name"] = faker.first_name()

        if "phone_number" not in kwargs:
            kwargs["phone_number"] = random_phone_number()

        if "status" not in kwargs:
            kwargs["status"] = STAFF_STATUS.ACTIVE

        return Staff.objects.create(**kwargs)


class APITestCaseV1_0(ModelsMixin, APITestCase):
    client_class = tests.APIClientV1_0  # type: ignore

    def render_page(
        self, count: int, results: List[Any], next: str = None, previous: str = None
    ) -> Dict[str, Any]:
        return {
            "count": count,
            "next": next,
            "previous": previous,
            "results": results,
        }

    def render_staff(self, staff: Staff) -> Dict[str, Any]:
        return {
            "staffId": str(staff.staff_id),
            "firstName": staff.first_name,
            "lastName": staff.last_name,
            "phoneNumber": staff.phone_number,
            "status": staff.status,
            "createdAt": dates.format_datetime(staff.created_at),
            "updatedAt": dates.format_datetime(staff.updated_at),
        }
