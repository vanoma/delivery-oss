from datetime import timedelta
import re
import json
import requests_mock
from django.test import override_settings
from django.utils import timezone
from rest_framework import status
from djangorestframework_camel_case.util import camelize  # type: ignore
from vanoma_api_utils.django.tests import load_fixture
from delivery_api.deliveries.background import assign_drivers_to_packages
from delivery_api.deliveries.models import Assignment
from delivery_api.deliveries.utils.constants import ASSIGNMENT_STATUS
from . import APITestCaseV1_0


@override_settings(
    VANOMA_ORDER_API_URL="http://order-api",
    VANOMA_COMMUNICATION_API_URL="http://communication-api",
)
class AssignDriversToAssingmentsTestCase(APITestCaseV1_0):
    def test_assign_drivers_to_packages__creates_one_assignment_per_driver(
        self,
    ) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.get(
                "http://order-api/packages?isAssigned=false&isAssignable=true&status=PLACED&sort=pickUpStart",
                json={
                    "results": [
                        load_fixture("package1.json"),
                        load_fixture("package2.json"),
                    ]
                },
            )
            mocker.get(
                "http://order-api/packages/19b34722-f0af-42ba-bd02-0bc7131fae96",
                json=load_fixture("package1.json"),
            )
            mocker.get(
                "https://maps.googleapis.com/maps/api/distancematrix/json",
                json=load_fixture("google-maps1.json"),
            )
            mocker.patch(
                re.compile("http://order-api/packages/*"),
                status_code=status.HTTP_200_OK,
            )
            mocker.put(
                re.compile("http://order-api/packages/*"),
                status_code=status.HTTP_200_OK,
            )
            mocker.post(
                re.compile("http://communication-api/*"),
                status_code=status.HTTP_200_OK,
            )

            drivers = [
                self.create_driver_with_location(is_available=False),
                self.create_driver_with_location(is_available=True),
            ]

            assign_drivers_to_packages()

            # Validate created assignment
            self.assertEqual(Assignment.objects.count(), 1)
            assignment = Assignment.objects.get(driver=drivers[1])
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.PENDING)
            self.assertEqual(
                assignment.package_id, "19b34722-f0af-42ba-bd02-0bc7131fae96"
            )

            # Validate request to update package
            actual_patch_body = json.loads(mocker.request_history[3].body)
            expected_patch_body = {
                "driverId": assignment.driver.driver_id,
                "assignmentId": assignment.assignment_id,
            }
            self.assertDictEqual(actual_patch_body, expected_patch_body)
            self.assertRegexpMatches(
                mocker.request_history[3].headers["Authorization"],
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.*",
            )

            # Validate request to create package event
            actual_event_body = json.loads(mocker.request_history[4].body)
            expected_event_body = {
                "eventName": "DRIVER_ASSIGNED",
                "assignmentId": assignment.assignment_id,
            }
            self.assertDictEqual(actual_event_body, expected_event_body)

            # Validate push notification
            actual_push_body = json.loads(mocker.request_history[5].body)
            expected_push_body = {
                "receiverIds": [str(drivers[1].driver_id)],
                "heading": "New Delivery!",
                "message": "Click to start a new assignment.",
                "jsonData": {
                    "notificationType": "NEW_ASSIGNMENT",
                },
                "metadata": {
                    "appId": None,
                    "apiKey": None,
                    "androidChannelId": None,
                },
            }
            self.assertDictEqual(actual_push_body, expected_push_body)

    def test_assign_drivers_to_packages__invalidates_pending_assignments(
        self,
    ) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.get(
                "http://order-api/packages?isAssigned=false&isAssignable=true&status=PLACED&sort=pickUpStart",
                json={"results": [load_fixture("package1.json")]},
            )
            mocker.patch(
                re.compile("http://order-api/packages/*"),
                status_code=status.HTTP_200_OK,
            )
            mocker.post(
                "http://communication-api/push",
                status_code=status.HTTP_200_OK,
            )

            assignments = [
                self.create_assignment(status=ASSIGNMENT_STATUS.PENDING),
                self.create_assignment(status=ASSIGNMENT_STATUS.PENDING),
                self.create_assignment(status=ASSIGNMENT_STATUS.PENDING),
            ]

            assignments[0].created_at = timezone.now() - timedelta(minutes=4)  # Expired
            assignments[0].save()
            assignments[1].created_at = timezone.now() - timedelta(minutes=3)  # Expired
            assignments[1].save()
            assignments[2].created_at = timezone.now() - timedelta(minutes=2)  # Not yet
            assignments[2].save()

            assign_drivers_to_packages()

            # Validate assignments
            assignments[0].refresh_from_db()
            self.assertEqual(assignments[0].status, ASSIGNMENT_STATUS.EXPIRED)

            assignments[1].refresh_from_db()
            self.assertEqual(assignments[1].status, ASSIGNMENT_STATUS.EXPIRED)

            assignments[2].refresh_from_db()
            self.assertEqual(assignments[2].status, ASSIGNMENT_STATUS.PENDING)

            # Validate request to update package
            actual_first_patch_body = json.loads(mocker.request_history[0].body)
            expected_first_patch_body = {"driverId": None, "assignmentId": None}
            self.assertDictEqual(actual_first_patch_body, expected_first_patch_body)
            self.assertRegexpMatches(
                mocker.request_history[0].headers["Authorization"],
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.*",
            )

            actual_second_patch_body = json.loads(mocker.request_history[1].body)
            expected_second_patch_body = {"driverId": None, "assignmentId": None}
            self.assertDictEqual(actual_second_patch_body, expected_second_patch_body)
            self.assertRegexpMatches(
                mocker.request_history[0].headers["Authorization"],
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.*",
            )

    def test_assign_drivers_to_packages__exludes_packages_not_ready_for_pickup(
        self,
    ) -> None:
        package_fixture = load_fixture("package1.json")
        package_fixture["pickUpStart"] = (
            timezone.now() + timedelta(minutes=21)
        ).isoformat()

        with requests_mock.Mocker() as mocker:
            mocker.get(
                "http://order-api/packages?isAssigned=false&isAssignable=true&status=PLACED&sort=pickUpStart",
                json={"results": [package_fixture]},
            )

            assign_drivers_to_packages()

            self.assertEqual(Assignment.objects.count(), 0)
