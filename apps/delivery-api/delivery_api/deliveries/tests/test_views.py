import re
import json
import requests_mock
from uuid import uuid4
from datetime import datetime, timedelta
from django.urls import reverse
from django.utils import timezone
from django.core.cache import cache
from django.test import override_settings
from rest_framework import status
from vanoma_api_utils.misc import create_api_error
from vanoma_api_utils.constants import ERROR_CODE
from vanoma_api_utils.django.tests import load_fixture
from djangorestframework_camel_case.util import camelize  # type: ignore
from delivery_api.deliveries.models import Assignment, Delay, Stop, Task
from delivery_api.deliveries.utils.constants import (
    ASSIGNMENT_STATUS,
    DELAY_STATUS,
    TASK_TYPE,
    CACHE_KEY,
)
from . import APITestCaseV1_0


@override_settings(VANOMA_ORDER_API_URL="http://order-api")
class DelayViewSetTestCaseV1_0(APITestCaseV1_0):
    def test_get_all_delays(self) -> None:
        delays = [self.create_delay(), self.create_delay()]

        url = reverse("delay-list")
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(
            2,
            [self.render_delay(delays[0]), self.render_delay(delays[1])],
        )
        self.assertDictEqual(actual, expected)

    def test_get_all_delays__filter_by_start_and_end_date(self) -> None:
        driver = self.create_driver()

        def create_older_delay(created_at: datetime) -> Delay:
            delay = self.create_delay(driver=driver)
            delay.created_at = created_at
            delay.save()
            return delay

        delays = [
            create_older_delay(timezone.now() - timedelta(days=7)),
            create_older_delay(timezone.now() - timedelta(days=4)),
            create_older_delay(timezone.now() - timedelta(days=1)),
        ]

        url = reverse("delay-list")
        start_date = (timezone.now() - timedelta(days=5)).date()
        end_date = (timezone.now() - timedelta(days=2)).date()

        response = self.client.get(f"{url}?startAt={start_date}&endAt={end_date}")
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(1, [self.render_delay(delays[1])])
        self.assertDictEqual(actual, expected)

    def test_get_all_delays__filter_by_status(self) -> None:
        delays = [
            self.create_delay(status=DELAY_STATUS.PENDING),
            self.create_delay(status=DELAY_STATUS.CONFIRMED),
        ]

        url = reverse("delay-list")
        response = self.client.get(f"{url}?status={DELAY_STATUS.PENDING}")
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(1, [self.render_delay(delays[0])])
        self.assertDictEqual(actual, expected)

    def test_update_delay(self) -> None:
        delay = self.create_delay()
        data = {
            "justification": "data:audio/mpeg;base64,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Hki1D0xVwuwJOIAAwwGBRULITIaJAVBS11MV//vkRNuACNRazFZ7gBEUiyk9z3AAKtXjQfndkIyWPSY/OcAJM0qVNm7u8UApqw4FkIXZKoJWszoSA5eHEUBbKHFcZ0H6pJU+zGYCdCVN2nptGuld9xYu+68cKa/F+vo/13GQ34XuD6fKs/9+/N0Mr3z8O/N6rVN56q0dej7qnscp4b5E6HPCnllPPU3c+f////Jcq/4f////9LZtAHVJRoCoBIVGt8bkUYiEQPstXOZHzMBwoMJQGMAwWLfmVaNgJfgoE6lIKAQKgAYsEwYGAwIgAMHQud9HYxtoMBITAhsxIgR6aWcIKAw6MUDjDyR2UETIJ9CSa4FiByNoNjBwxgMCKLQ1Nm6DhnYAZUnGhFhqpeW6TRR9lCeso3jjKQYLGTAxoYyVgpcQy8jWK12AV2rC3f3jreJjYSvcw0ZMhGQMRjAKYuNrlRNpoJUxfaT63jrf+Y2ClYYYKImKgZjgAmWChcxwMfWJQ9nEneh6a/////0sFdmIBhhQAvQAAhhgUEBClRgwZGn+1Kn+xuQ1Vvf/////+BhgmCwQDAYQBw+o8GABiIKqNrhhIOBgtk9NVpdVYzS1YzGdymGf////////fdibrxlh7T20gNx2CTsvZw7liIORFKaMSyfw5//////+6XLtWta7WpqbVam/VNtiKAUBSguSQaMxtXXA0HA0GQZSbp1W2FQEmMhyYKEYCBBrB4igYMNDxAUkE5Jrs8mRxSSgEw4L2lNjMwh8wasSwDTHhNSpcQ44JDCSHDCSY2CbVTAwPp5TUMGAZgxoMPgYAsqbvf5WFgWmEYnCZhkDpeAgAsib5SluV3H9+l+imgkf1Y5fBMKBm5IrJFQV///+4lpLxYj8OgoA7TDuRJlUpsf///+yR5lyMEZO3Briu1zOi4sebixGHYB/////4tFFjv3acNicDwCwN/X9uwy7OUy7Na///////7jwhw3HbShftlk5Ts4dyxKa1rdampq1NGtU0a////////+pSWMqSxfqWJfqxL8/p6fPdPbvV7f//////6pseVsbP40uO6Wl+rS/ulyS1UxBTUUzLjEwMFVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV//sUZFOP8AAAf4cAAAgAAA/w4AABAAABpAAAACAAADSAAAAEVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV"
        }

        url = reverse("delay-detail", args=(delay.delay_id,))
        response = self.client.patch(url, data=data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        delay.refresh_from_db()
        actual = json.loads(response.content)
        expected = self.render_delay(delay)
        self.assertDictEqual(actual, expected)


@override_settings(
    VANOMA_ORDER_API_URL="http://order-api",
    VANOMA_COMMUNICATION_API_URL="http://communication-api",
)
class AssignmentViewSetTestCaseV1_0(APITestCaseV1_0):
    def test_get_all_assignments(self) -> None:
        drivers = [self.create_driver(), self.create_driver()]
        assignments = [
            self.create_assignment(driver=drivers[0]),
            self.create_confirmed_assignment(driver=drivers[1]),
        ]
        stops = [
            self.create_stop(driver=drivers[0], ranking=0),
            self.create_stop(driver=drivers[0], ranking=1),
            self.create_stop(driver=drivers[1], ranking=0),
            self.create_stop(driver=drivers[1], ranking=1),
        ]
        tasks = [
            self.create_task(
                type=TASK_TYPE.PICK_UP, stop=stops[0], assignment=assignments[0]
            ),
            self.create_task(
                type=TASK_TYPE.DROP_OFF, stop=stops[1], assignment=assignments[0]
            ),
            self.create_task(
                type=TASK_TYPE.PICK_UP, stop=stops[1], assignment=assignments[1]
            ),
            self.create_task(
                type=TASK_TYPE.DROP_OFF, stop=stops[2], assignment=assignments[1]
            ),
        ]

        url = reverse("assignment-list")
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(
            2,
            [
                self.render_assignment(assignments[0]),
                self.render_assignment(assignments[1]),
            ],
        )
        self.assertDictEqual(actual, expected)

    def test_get_all_assignments__filter_by_assignment_id(self) -> None:
        drivers = [self.create_driver(), self.create_driver()]
        assignments = [
            self.create_assignment(driver=drivers[0]),
            self.create_confirmed_assignment(driver=drivers[1]),
        ]
        stops = [
            self.create_stop(driver=drivers[0], ranking=0),
            self.create_stop(driver=drivers[0], ranking=1),
            self.create_stop(driver=drivers[1], ranking=0),
            self.create_stop(driver=drivers[1], ranking=1),
        ]
        tasks = [
            self.create_task(
                type=TASK_TYPE.PICK_UP, stop=stops[0], assignment=assignments[0]
            ),
            self.create_task(
                type=TASK_TYPE.DROP_OFF, stop=stops[1], assignment=assignments[0]
            ),
            self.create_task(
                type=TASK_TYPE.PICK_UP, stop=stops[1], assignment=assignments[1]
            ),
            self.create_task(
                type=TASK_TYPE.DROP_OFF, stop=stops[2], assignment=assignments[1]
            ),
        ]

        url = reverse("assignment-list")
        response = self.client.get(f"{url}?assignmentId={assignments[1].assignment_id}")
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(1, [self.render_assignment(assignments[1])])
        self.assertDictEqual(actual, expected)

    def test_get_all_assignments__filter_by_package_id(self) -> None:
        package_ids = [str(uuid4()), str(uuid4()), str(uuid4())]
        assignments = [
            self.create_assignment(package_id=package_ids[0]),
            self.create_assignment(package_id=package_ids[1]),
            self.create_assignment(package_id=package_ids[2]),
        ]

        url = reverse("assignment-list")
        response = self.client.get(f"{url}?packageId={package_ids[0]}")
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(1, [self.render_assignment(assignments[0])])
        self.assertDictEqual(actual, expected)

    def test_create_assignment(self) -> None:
        package_id = "19b34722-f0af-42ba-bd02-0bc7131fae96"
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages?packageId={package_id}",
                json={"results": [load_fixture("package1.json")]},
            )
            mocker.get(
                f"http://order-api/packages/{package_id}",
                json=load_fixture("package1.json"),
            )
            mocker.get(
                "https://maps.googleapis.com/maps/api/distancematrix/json",
                json=load_fixture("google-maps1.json"),
            )
            mocker.patch(
                f"http://order-api/packages/{package_id}",
                status_code=status.HTTP_200_OK,
            )
            mocker.put(
                f"http://order-api/packages/{package_id}/events",
                status_code=status.HTTP_200_OK,
            )
            mocker.post(
                re.compile("http://communication-api/*"),
                status_code=status.HTTP_200_OK,
            )

            driver = self.create_driver_with_location()

            url = reverse("assignment-list")
            response = self.client.post(
                url, data={"driverId": driver.driver_id, "packageId": package_id}
            )
            self.assertEqual(response.status_code, status.HTTP_201_CREATED)

            assignment = Assignment.objects.get(package_id=package_id, driver=driver)

            # Validate response
            actual = json.loads(response.content)
            expected = self.render_assignment(assignment)
            self.assertDictEqual(actual, expected)

            # Validate assignment
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.PENDING)
            self.assertIsNotNone(assignment.package_id, package_id)

            # Validate stops
            self.assertEqual(Stop.objects.count(), 2)

            assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=assignment
            ).order_by("created_at")

            self.assertEqual(assignment_stops[0].ranking, 0)
            self.assertEqual(assignment_stops[0].tasks.count(), 1)
            self.assertEqual(assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(assignment_stops[1].ranking, 1)
            self.assertEqual(assignment_stops[1].tasks.count(), 1)
            self.assertEqual(assignment_stops[1].latitude, -1.935355)
            self.assertEqual(assignment_stops[1].longitude, 30.0444221)

            # Validate tasks
            self.assertEqual(Task.objects.count(), 2)

            first_stop_tasks = assignment_stops[0].tasks.all()
            second_stop_tasks = assignment_stops[1].tasks.all()

            self.assertEqual(len(first_stop_tasks), 1)
            self.assertEqual(first_stop_tasks[0].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(second_stop_tasks), 1)
            self.assertEqual(second_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

            # Validate request to update package
            actual_patch_body = json.loads(mocker.request_history[1].body)
            expected_patch_body = {
                "driverId": assignment.driver.driver_id,
                "assignmentId": str(assignment.assignment_id),
            }
            self.assertDictEqual(actual_patch_body, expected_patch_body)
            self.assertRegexpMatches(
                mocker.request_history[1].headers["Authorization"],
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.*",
            )

            # Validate request to create package event
            actual_event_body = json.loads(mocker.request_history[2].body)
            expected_event_body = {
                "eventName": "DRIVER_ASSIGNED",
                "assignmentId": str(assignment.assignment_id),
            }
            self.assertDictEqual(actual_event_body, expected_event_body)

            # Validate push notification
            actual_push_body = json.loads(mocker.request_history[3].body)
            expected_push_body = {
                "receiverIds": [str(driver.driver_id)],
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

    def test_create_assignment__returns_error_if_package_is_assigned(self) -> None:
        driver = self.create_driver()
        assignment = self.create_assignment()

        url = reverse("assignment-list")
        response = self.client.post(
            url, data={"driverId": driver.driver_id, "packageId": assignment.package_id}
        )

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        actual = json.loads(response.content)
        expected = {
            "errorCode": "INVALID_REQUEST",
            "errorMessage": "Package is already assigned. Cancel existing assignment first.",
        }
        self.assertDictEqual(actual, expected)

    def test_create_assignment__returns_error_if_assignment_task_is_running(
        self,
    ) -> None:
        driver = self.create_driver()
        cache.set(CACHE_KEY.IS_AUTO_ASSIGNMENT_RUNNING, True)

        url = reverse("assignment-list")
        response = self.client.post(
            url, data={"driverId": driver.driver_id, "packageId": str(uuid4())}
        )

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        actual = json.loads(response.content)
        expected = {
            "errorCode": "INVALID_REQUEST",
            "errorMessage": "Automatic assignment task is running. Please wait a few seconds for the task to complete.",
        }
        self.assertDictEqual(actual, expected)

        cache.delete(CACHE_KEY.IS_AUTO_ASSIGNMENT_RUNNING)


@override_settings(
    VANOMA_ORDER_API_URL="http://order-api",
    VANOMA_COMMUNICATION_API_URL="http://communication-api",
)
class CurrentAssignmentViewSetTestCaseV1_0(APITestCaseV1_0):
    def test_get_all_current_assignments(self) -> None:
        package_ids = [
            "19b34722-f0af-42ba-bd02-0bc7131fae96",
            "23876d54-78a8-4757-9090-bf4567ad12f2",
        ]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages?packageId={package_ids[0]},{package_ids[1]}",
                json={
                    "results": [
                        load_fixture("package1.json"),
                        load_fixture("package2.json"),
                    ]
                },
            )

            drivers = [self.create_driver(), self.create_driver()]
            assignments = [
                self.create_assignment(driver=drivers[0], package_id=package_ids[0]),
                self.create_confirmed_assignment(
                    driver=drivers[1], package_id=package_ids[1]
                ),
            ]
            stops = [
                self.create_stop(driver=drivers[0], ranking=0),
                self.create_stop(driver=drivers[0], ranking=1),
                self.create_stop(driver=drivers[1], ranking=0),
                self.create_stop(driver=drivers[1], ranking=1),
            ]
            tasks = [
                self.create_task(
                    type=TASK_TYPE.PICK_UP, stop=stops[0], assignment=assignments[0]
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF, stop=stops[1], assignment=assignments[0]
                ),
                self.create_task(
                    type=TASK_TYPE.PICK_UP, stop=stops[1], assignment=assignments[1]
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF, stop=stops[2], assignment=assignments[1]
                ),
            ]

            url = reverse("current-assignment-list")
            response = self.client.get(url)
            self.assertEqual(response.status_code, status.HTTP_200_OK)

            actual = json.loads(response.content)
            expected = self.render_page(
                2,
                [
                    self.render_current_assignment(assignments[0], "package1.json"),
                    self.render_current_assignment(assignments[1], "package2.json"),
                ],
            )
            self.assertDictEqual(actual, expected)

    def test_get_all_current_assignments__filter_by_status(self) -> None:
        package_ids = [
            "19b34722-f0af-42ba-bd02-0bc7131fae96",
            "23876d54-78a8-4757-9090-bf4567ad12f2",
        ]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages?packageId={package_ids[1]}",
                json={"results": [load_fixture("package2.json")]},
            )

            drivers = [self.create_driver(), self.create_driver()]
            assignments = [
                self.create_assignment(driver=drivers[0], package_id=package_ids[0]),
                self.create_confirmed_assignment(
                    driver=drivers[1], package_id=package_ids[1]
                ),
            ]
            stops = [
                self.create_stop(driver=drivers[0], ranking=0),
                self.create_stop(driver=drivers[0], ranking=1),
                self.create_stop(driver=drivers[1], ranking=0),
                self.create_stop(driver=drivers[1], ranking=1),
            ]
            tasks = [
                self.create_task(
                    type=TASK_TYPE.PICK_UP, stop=stops[0], assignment=assignments[0]
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF, stop=stops[1], assignment=assignments[0]
                ),
                self.create_task(
                    type=TASK_TYPE.PICK_UP, stop=stops[1], assignment=assignments[1]
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF, stop=stops[2], assignment=assignments[1]
                ),
            ]

            url = reverse("current-assignment-list")
            response = self.client.get(f"{url}?status={ASSIGNMENT_STATUS.CONFIRMED}")
            self.assertEqual(response.status_code, status.HTTP_200_OK)

            actual = json.loads(response.content)
            expected = self.render_page(
                1, [self.render_current_assignment(assignments[1], "package2.json")]
            )
            self.assertDictEqual(actual, expected)

    def test_cancel_assignment__with_confirmed_assignment(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.patch(
                re.compile("http://order-api/packages/*"),
                status_code=status.HTTP_200_OK,
            )
            mocker.post(
                "http://communication-api/push",
                status_code=status.HTTP_200_OK,
            )

            driver = self.create_driver()
            assignment = self.create_confirmed_assignment(driver=driver)
            stops = [
                self.create_stop(driver=driver),
                self.create_stop(driver=driver),
            ]
            tasks = [
                self.create_task(
                    stop=stops[0], assignment=assignment, type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[1], assignment=assignment, type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse(
                "current-assignment-cancellation", args=(assignment.assignment_id,)
            )
            response = self.client.post(
                url, data={"packageIds": [assignment.package_id]}
            )
            self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

            # Validate assignment
            assignment.refresh_from_db()
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.CANCELED)
            self.assertIsNotNone(assignment.confirmed_at)
            self.assertIsNotNone(assignment.confirmation_location)

            # Validate tasks
            self.assertFalse(Task.objects.exists())

            # Validate stops
            self.assertFalse(Stop.objects.exists())

            # Validate request to update package
            actual_patch_body = json.loads(mocker.request_history[0].body)
            expected_patch_body = {"driverId": None, "assignmentId": None}
            self.assertDictEqual(actual_patch_body, expected_patch_body)
            self.assertRegexpMatches(
                mocker.request_history[0].headers["Authorization"],
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.*",
            )

            # Validate push notification
            actual_first_push_body = json.loads(mocker.request_history[1].body)
            expected_first_push_body = {
                "receiverIds": [str(driver.driver_id)],
                "heading": "CANCELED DELIVERY.",
                "message": "This delivery has been canceled.",
                "jsonData": {
                    "notificationType": "CANCELLED_ASSIGNMENT",
                },
                "metadata": {
                    "appId": None,
                    "apiKey": None,
                    "androidChannelId": None,
                },
            }
            self.assertDictEqual(actual_first_push_body, expected_first_push_body)

    def test_cancel_assignment__with_pending_assignment(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.patch(
                re.compile("http://order-api/packages/*"),
                status_code=status.HTTP_200_OK,
            )
            mocker.post(
                "http://communication-api/push",
                status_code=status.HTTP_200_OK,
            )

            driver = self.create_driver()
            assignment = self.create_assignment(driver=driver)
            stops = [
                self.create_stop(driver=driver),
                self.create_stop(driver=driver),
            ]
            tasks = [
                self.create_task(
                    stop=stops[0], assignment=assignment, type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[1], assignment=assignment, type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse(
                "current-assignment-cancellation", args=(assignment.assignment_id,)
            )
            response = self.client.post(
                url, data={"packageIds": [assignment.package_id]}
            )
            self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

            # Validate assignment
            assignment.refresh_from_db()
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.CANCELED)

            # Validate tasks
            self.assertFalse(Task.objects.exists())

            # Validate stops
            self.assertFalse(Stop.objects.exists())

            # Validate request to update package
            actual_patch_body = json.loads(mocker.request_history[0].body)
            expected_patch_body = {"driverId": None, "assignmentId": None}
            self.assertDictEqual(actual_patch_body, expected_patch_body)
            self.assertRegexpMatches(
                mocker.request_history[0].headers["Authorization"],
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.*",
            )

            # Validate push notification
            actual_first_push_body = json.loads(mocker.request_history[1].body)
            expected_first_push_body = {
                "receiverIds": [str(driver.driver_id)],
                "heading": "CANCELED DELIVERY.",
                "message": "This delivery has been canceled.",
                "jsonData": {
                    "notificationType": "CANCELLED_ASSIGNMENT",
                },
                "metadata": {
                    "appId": None,
                    "apiKey": None,
                    "androidChannelId": None,
                },
            }
            self.assertDictEqual(actual_first_push_body, expected_first_push_body)

    def test_cancel_assignment__returns_error_for_completed_assignment(self) -> None:
        assignment = self.create_assignment(status=ASSIGNMENT_STATUS.COMPLETED)

        url = reverse(
            "current-assignment-cancellation", args=(assignment.assignment_id,)
        )
        response = self.client.post(url, data={"packageIds": [assignment.package_id]})
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

        assignment.refresh_from_db()
        self.assertEqual(assignment.status, ASSIGNMENT_STATUS.COMPLETED)
        actual = json.loads(response.content)
        expected = {
            "errorCode": "RESOURCE_NOT_FOUND",
            "errorMessage": "No Assignment matches the given query.",
        }
        self.assertDictEqual(actual, expected)

    def test_cancel_assignment__returns_error_for_expired_assignment(self) -> None:
        assignment = self.create_assignment(status=ASSIGNMENT_STATUS.EXPIRED)

        url = reverse(
            "current-assignment-cancellation", args=(assignment.assignment_id,)
        )
        response = self.client.post(url, data={"packageIds": [assignment.package_id]})
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

        assignment.refresh_from_db()
        self.assertEqual(assignment.status, ASSIGNMENT_STATUS.EXPIRED)
        actual = json.loads(response.content)
        expected = {
            "errorCode": "RESOURCE_NOT_FOUND",
            "errorMessage": "No Assignment matches the given query.",
        }
        self.assertDictEqual(actual, expected)

    def test_cancel_assignment__returns_error_for_cancelled_assignment(self) -> None:
        assignment = self.create_assignment(status=ASSIGNMENT_STATUS.CANCELED)

        url = reverse(
            "current-assignment-cancellation", args=(assignment.assignment_id,)
        )
        response = self.client.post(url, data={"packageIds": [assignment.package_id]})
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

        assignment.refresh_from_db()
        self.assertEqual(assignment.status, ASSIGNMENT_STATUS.CANCELED)
        actual = json.loads(response.content)
        expected = {
            "errorCode": "RESOURCE_NOT_FOUND",
            "errorMessage": "No Assignment matches the given query.",
        }
        self.assertDictEqual(actual, expected)


@override_settings(VANOMA_ORDER_API_URL="http://order-api")
class CurrentStopViewSetTestCaseV1_0(APITestCaseV1_0):
    def test_get_all_current_stops(self) -> None:
        package_ids = [
            "19b34722-f0af-42ba-bd02-0bc7131fae96",
            "1e74abdf-9026-42ef-838b-31f6c9c8f2ad",
        ]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages?packageId={package_ids[0]},{package_ids[1]}",
                json={
                    "results": [
                        load_fixture("package1.json"),
                        load_fixture("package3.json"),
                    ]
                },
            )

            drivers = [self.create_driver(), self.create_driver()]
            stops = [
                self.create_stop(driver=drivers[0], ranking=0),
                self.create_stop(driver=drivers[0], ranking=1),
                self.create_stop(driver=drivers[1], ranking=0),
                self.create_stop(driver=drivers[1], ranking=1),
            ]
            assignments = [
                self.create_confirmed_assignment(
                    driver=drivers[0], package_id=package_ids[0]
                ),
                self.create_confirmed_assignment(
                    driver=drivers[1], package_id=package_ids[1]
                ),
            ]
            tasks = [
                self.create_task(
                    stop=stops[0], assignment=assignments[0], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[1], assignment=assignments[0], type=TASK_TYPE.DROP_OFF
                ),
                self.create_task(
                    stop=stops[2], assignment=assignments[1], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[3], assignment=assignments[1], type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse("current-stop-list")
            response = self.client.get(url)
            self.assertEqual(response.status_code, status.HTTP_200_OK)

            actual = json.loads(response.content)
            expected = self.render_page(
                4,
                [
                    self.render_current_stop(stops[0], "package1.json"),
                    self.render_current_stop(stops[1], "package1.json"),
                    self.render_current_stop(stops[2], "package3.json"),
                    self.render_current_stop(stops[3], "package3.json"),
                ],
            )
            self.assertDictEqual(actual, expected)

    def test_get_all_current_stops__exclude_stops_for_pending_assignments(self) -> None:
        package_ids = [
            "19b34722-f0af-42ba-bd02-0bc7131fae96",
            "1e74abdf-9026-42ef-838b-31f6c9c8f2ad",
        ]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages?packageId={package_ids[0]}",
                json={
                    "results": [
                        load_fixture("package1.json"),
                        load_fixture("package3.json"),
                    ]
                },
            )

            driver = self.create_driver()
            stops = [
                self.create_stop(driver=driver, ranking=0),
                self.create_stop(driver=driver, ranking=1),
                self.create_stop(driver=driver, ranking=2),
            ]
            assignments = [
                self.create_confirmed_assignment(
                    driver=driver, package_id=package_ids[0]
                ),
                self.create_assignment(driver=driver, package_id=package_ids[1]),
            ]
            tasks = [  # First & Second Pickup -> First Dropoff -> Second Dropoff
                self.create_task(
                    stop=stops[0], assignment=assignments[0], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[0], assignment=assignments[1], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[1], assignment=assignments[1], type=TASK_TYPE.DROP_OFF
                ),
                self.create_task(
                    stop=stops[2], assignment=assignments[0], type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse("current-stop-list")
            response = self.client.get(url)
            self.assertEqual(response.status_code, status.HTTP_200_OK)

            actual = json.loads(response.content)
            expected = self.render_page(
                2,
                [
                    self.render_current_stop(stops[0], "package1.json"),
                    self.render_current_stop(stops[2], "package1.json"),
                ],
            )
            self.assertDictEqual(actual, expected)

    def test_get_all_current_stops__returns_error_if_fetching_packages_failed(
        self,
    ) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.get(
                re.compile("http://order-api/packages*"),
                status_code=status.HTTP_400_BAD_REQUEST,
                json=camelize(
                    create_api_error(
                        ERROR_CODE.INVALID_REQUEST, "Invalid fetch package request"
                    )
                ),
            )

            driver = self.create_driver()
            stops = [
                self.create_stop(driver=driver, ranking=0),
                self.create_stop(driver=driver, ranking=1),
            ]
            assignments = [self.create_confirmed_assignment(driver=driver)]
            tasks = [
                self.create_task(
                    stop=stops[0], assignment=assignments[0], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[1], assignment=assignments[0], type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse("current-stop-list")
            response = self.client.get(url)
            self.assertEqual(
                response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR
            )

            actual = json.loads(response.content)
            expected = {
                "errorCode": "INTERNAL_ERROR",
                "errorMessage": "Unable to fetch packages from order-api: {'errorCode': 'INVALID_REQUEST', 'errorMessage': 'Invalid fetch package request'}",
            }
            self.assertDictEqual(actual, expected)

    def test_departure(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.put(
                requests_mock.ANY,
                status_code=status.HTTP_200_OK,
            )

            stop = self.create_stop()
            assignments = [
                self.create_confirmed_assignment(),
                self.create_confirmed_assignment(),
            ]
            tasks = [
                self.create_task(
                    stop=stop, assignment=assignments[0], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stop, assignment=assignments[0], type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse(
                "current-stop-departure",
                args=(stop.stop_id,),
            )

            response = self.client.post(url, data=None)
            self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

            # Validate stop
            stop.refresh_from_db()
            self.assertIsNotNone(stop.departed_at)

            # Validate request to create package event for pickup task
            actual_first_body = json.loads(mocker.request_history[0].body)
            expected_first_body = {
                "eventName": "DRIVER_DEPARTING_PICK_UP",
                "assignmentId": str(assignments[0].assignment_id),
            }
            self.assertDictEqual(actual_first_body, expected_first_body)

            # Validate request to create package event for dropoff task
            actual_first_body = json.loads(mocker.request_history[1].body)
            expected_first_body = {
                "eventName": "DRIVER_DEPARTING_DROP_OFF",
                "assignmentId": str(assignments[0].assignment_id),
            }
            self.assertDictEqual(actual_first_body, expected_first_body)

    def test_arrival(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.put(
                requests_mock.ANY,
                status_code=status.HTTP_200_OK,
            )

            stop = self.create_stop(
                depart_by=timezone.now() - timedelta(minutes=20),
                arrive_by=timezone.now() + timedelta(minutes=5),
                departed_at=timezone.now() - timedelta(minutes=20),
            )
            assignments = [self.create_confirmed_assignment()]
            tasks = [
                self.create_task(
                    stop=stop, assignment=assignments[0], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stop, assignment=assignments[0], type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse(
                "current-stop-arrival",
                args=(stop.stop_id,),
            )

            response = self.client.post(url, data=None)
            self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

            # Validate stop
            stop.refresh_from_db()
            self.assertIsNotNone(stop.arrived_at)

            # Validate delay
            self.assertFalse(Delay.objects.exists())

            # Validate request to create package event for pickup task
            actual_first_body = json.loads(mocker.request_history[0].body)
            expected_first_body = {
                "eventName": "DRIVER_ARRIVED_PICK_UP",
                "assignmentId": str(assignments[0].assignment_id),
            }
            self.assertDictEqual(actual_first_body, expected_first_body)

            # Validate request to create package event for dropoff task
            actual_first_body = json.loads(mocker.request_history[1].body)
            expected_first_body = {
                "eventName": "DRIVER_ARRIVED_DROP_OFF",
                "assignmentId": str(assignments[0].assignment_id),
            }
            self.assertDictEqual(actual_first_body, expected_first_body)

    def test_arrival__with_breached_expected_duration(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.put(
                requests_mock.ANY,
                status_code=status.HTTP_200_OK,
            )

            stop = self.create_stop(
                depart_by=timezone.now() - timedelta(minutes=20),
                arrive_by=timezone.now() - timedelta(minutes=5),
                departed_at=timezone.now() - timedelta(minutes=20),
            )
            assignments = [self.create_confirmed_assignment()]
            tasks = [
                self.create_task(
                    stop=stop, assignment=assignments[0], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stop, assignment=assignments[0], type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse(
                "current-stop-arrival",
                args=(stop.stop_id,),
            )

            response = self.client.post(url, data=None)
            self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

            # Validate stop
            stop.refresh_from_db()
            self.assertIsNotNone(stop.arrived_at)

            # Validate delay
            self.assertTrue(Delay.objects.exists())

            # Validate request to create package event for pickup task
            actual_first_body = json.loads(mocker.request_history[0].body)
            expected_first_body = {
                "eventName": "DRIVER_ARRIVED_PICK_UP",
                "assignmentId": str(assignments[0].assignment_id),
            }
            self.assertDictEqual(actual_first_body, expected_first_body)

            # Validate request to create package event for dropoff task
            actual_first_body = json.loads(mocker.request_history[1].body)
            expected_first_body = {
                "eventName": "DRIVER_ARRIVED_DROP_OFF",
                "assignmentId": str(assignments[0].assignment_id),
            }
            self.assertDictEqual(actual_first_body, expected_first_body)


@override_settings(VANOMA_ORDER_API_URL="http://order-api")
class CurrentTaskCompletionViewTestCaseV1_0(APITestCaseV1_0):
    def test_completion__with_pickup_task(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.put(
                requests_mock.ANY,
                status_code=status.HTTP_200_OK,
            )

            assignment = self.create_confirmed_assignment()
            tasks = [
                self.create_task(
                    assignment=assignment,
                    type=TASK_TYPE.PICK_UP,
                    completed_at=None,
                ),
                self.create_task(
                    assignment=assignment,
                    type=TASK_TYPE.DROP_OFF,
                    completed_at=None,
                ),
            ]

            url = reverse("current-task-completion", args=(str(tasks[0].task_id),))
            response = self.client.post(url, data=None)
            self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

            # Validate task
            tasks[0].refresh_from_db()
            self.assertIsNotNone(tasks[0].completed_at)

            # Validate assignment
            assignment.refresh_from_db()
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.CONFIRMED)

            # Validate stop
            self.assertIsNotNone(tasks[0].stop.completed_at)

            # Validate request to create package event for pickup task
            actual_first_body = json.loads(mocker.request_history[0].body)
            expected_first_body = {
                "eventName": "PACKAGE_PICKED_UP",
                "assignmentId": assignment.assignment_id,
            }
            self.assertDictEqual(actual_first_body, expected_first_body)

    def test_completion__with_dropoff_task(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.put(
                requests_mock.ANY,
                status_code=status.HTTP_200_OK,
            )

            assignment = self.create_confirmed_assignment()
            tasks = [
                self.create_task(
                    assignment=assignment,
                    type=TASK_TYPE.PICK_UP,
                    completed_at=timezone.now(),
                ),
                self.create_task(
                    assignment=assignment,
                    type=TASK_TYPE.DROP_OFF,
                    completed_at=None,
                ),
            ]

            url = reverse("current-task-completion", args=(tasks[1].task_id,))
            response = self.client.post(url, data=None)
            self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

            # Validate task
            tasks[1].refresh_from_db()
            self.assertIsNotNone(tasks[1].completed_at)

            # Validate assignment
            assignment.refresh_from_db()
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.COMPLETED)

            # Validate stop
            self.assertIsNotNone(tasks[1].stop.completed_at)

            # Validate request to create package event for dropoff task
            actual_first_body = json.loads(mocker.request_history[0].body)
            expected_first_body = {
                "eventName": "PACKAGE_DELIVERED",
                "assignmentId": assignment.assignment_id,
            }
            self.assertDictEqual(actual_first_body, expected_first_body)

    def test_completion__returns_error_if_task_is_completed(self) -> None:
        assignment = self.create_confirmed_assignment()
        tasks = [
            self.create_task(
                assignment=assignment,
                type=TASK_TYPE.PICK_UP,
                completed_at=timezone.now(),
            ),
            self.create_task(
                assignment=assignment,
                type=TASK_TYPE.DROP_OFF,
                completed_at=None,
            ),
        ]

        url = reverse("current-task-completion", args=(str(tasks[0].task_id),))
        response = self.client.post(url, data=None)
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

        actual_payload = json.loads(response.content)
        expected_payload = {
            "errorCode": "INVALID_REQUEST",
            "errorMessage": "Task is already completed",
        }
        self.assertDictEqual(actual_payload, expected_payload)

    def test_completion__returns_error_if_assignment_status_is_not_confirmed(
        self,
    ) -> None:
        assignment = self.create_assignment()
        tasks = [
            self.create_task(
                assignment=assignment,
                type=TASK_TYPE.PICK_UP,
                completed_at=None,
            ),
            self.create_task(
                assignment=assignment,
                type=TASK_TYPE.DROP_OFF,
                completed_at=None,
            ),
        ]

        url = reverse("current-task-completion", args=(str(tasks[0].task_id),))
        response = self.client.post(url, data=None)
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

        actual_payload = json.loads(response.content)
        expected_payload = {
            "errorCode": "RESOURCE_NOT_FOUND",
            "errorMessage": "Task matching query does not exist.",
        }
        self.assertDictEqual(actual_payload, expected_payload)


@override_settings(VANOMA_ORDER_API_URL="http://order-api")
class AssignmentConfirmationViewTestCaseV1_0(APITestCaseV1_0):
    def test_confirmation(self) -> None:
        package_id = str(uuid4())
        with requests_mock.Mocker() as mocker:
            mocker.put(
                f"http://order-api/packages/{package_id}/events",
                status_code=status.HTTP_200_OK,
            )
            mocker.get(
                "https://maps.googleapis.com/maps/api/distancematrix/json",
                json=load_fixture("google-maps1.json"),
            )

            driver = self.create_driver()
            location = self.create_location(driver=driver, is_assigned=False)
            assignment = self.create_assignment(driver=driver, package_id=package_id)
            stops = [
                self.create_stop(driver=driver),
                self.create_stop(driver=driver),
            ]
            tasks = [
                self.create_task(
                    stop=stops[0], assignment=assignment, type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[1], assignment=assignment, type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse("assignment-confirmation-list")
            response = self.client.post(
                url,
                data={
                    "driver_id": driver.driver_id,
                    "assignment_ids": [assignment.assignment_id],
                },
            )

            self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

            # Validate location
            location.refresh_from_db()
            self.assertTrue(location.is_assigned)

            # Validate assignment
            assignment.refresh_from_db()
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.CONFIRMED)
            self.assertEqual(assignment.confirmation_location, location)
            self.assertIsNotNone(assignment.confirmed_at)

            # Validate request to create package event
            actual_post_body = json.loads(mocker.request_history[0].body)
            expected_post_body = {
                "eventName": "DRIVER_CONFIRMED",
                "assignmentId": assignment.assignment_id,
            }
            self.assertDictEqual(actual_post_body, expected_post_body)

            # Validate stops
            for stop in stops:
                stop.refresh_from_db()
                self.assertIsNotNone(stop.depart_by)
                self.assertIsNotNone(stop.arrive_by)

    def test_confirmation__returns_error_for_non_pending_assignment(self) -> None:
        driver = self.create_driver()
        assignment = self.create_confirmed_assignment(driver=driver)
        stops = [
            self.create_stop(driver=driver, ranking=0),
            self.create_stop(driver=driver, ranking=1),
        ]
        tasks = [
            self.create_task(
                type=TASK_TYPE.PICK_UP, stop=stops[0], assignment=assignment
            ),
            self.create_task(
                type=TASK_TYPE.DROP_OFF, stop=stops[1], assignment=assignment
            ),
        ]

        url = reverse("assignment-confirmation-list")
        response = self.client.post(
            url,
            data={
                "driver_id": driver.driver_id,
                "assignment_ids": [assignment.assignment_id],
            },
        )

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

        actual = json.loads(response.content)
        expected = {
            "errorCode": "INVALID_REQUEST",
            "errorMessage": "Assignment is no longer valid.",
        }
        self.assertDictEqual(actual, expected)

    def test_confirmation__returns_error_if_missing_driver_latest_location(
        self,
    ) -> None:
        driver = self.create_driver()
        assignment = self.create_assignment(driver=driver)
        stops = [
            self.create_stop(driver=driver, ranking=0),
            self.create_stop(driver=driver, ranking=1),
        ]
        tasks = [
            self.create_task(
                type=TASK_TYPE.PICK_UP, stop=stops[0], assignment=assignment
            ),
            self.create_task(
                type=TASK_TYPE.DROP_OFF, stop=stops[1], assignment=assignment
            ),
        ]

        url = reverse("assignment-confirmation-list")
        response = self.client.post(
            url,
            data={
                "driver_id": driver.driver_id,
                "assignment_ids": [assignment.assignment_id],
            },
        )

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

        actual = json.loads(response.content)
        expected = {
            "errorCode": "INVALID_REQUEST",
            "errorMessage": "Driver does not have latest location",
        }
        self.assertDictEqual(actual, expected)
