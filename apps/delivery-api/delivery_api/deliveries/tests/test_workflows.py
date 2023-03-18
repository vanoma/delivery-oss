import re
import requests_mock
from django.utils import timezone
from django.test import override_settings
from rest_framework import status
from vanoma_api_utils.misc import create_api_error
from vanoma_api_utils.constants import ERROR_CODE
from vanoma_api_utils.django.tests import load_fixture
from djangorestframework_camel_case.util import camelize, underscoreize  # type: ignore
from delivery_api.deliveries.models import Assignment, Stop, Task
from delivery_api.deliveries.workflows import (
    invalidate_assignments,
    create_assignments,
    find_assignable_driver,
)
from delivery_api.deliveries.utils.constants import (
    ASSIGNMENT_STATUS,
    ASSIGNMENT_TYPE,
    TASK_TYPE,
)
from delivery_api.drivers.utils.constants import DRIVER_STATUS
from . import APITestCaseV1_0


"""
Some of the workflows are used in different views, and have different branches. So 
instead of replicating tests for all the possible branches of a given workflow logic, 
it makes sense to unit-test the workflow logic on its own.
"""


@override_settings(
    VANOMA_ORDER_API_URL="http://order-api",
    VANOMA_COMMUNICATION_API_URL="http://communication-api",
)
class InvalidateAssignmentTestCase(APITestCaseV1_0):
    def test_invalidate_assignment__using_pending_assignment(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.patch(
                re.compile("http://order-api/packages/*"),
                status_code=status.HTTP_200_OK,
            )
            mocker.post(
                re.compile("http://communication-api/*"),
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

            invalidate_assignments(ASSIGNMENT_STATUS.EXPIRED, [assignment])

            # Validate assignment
            assignment.refresh_from_db()
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.EXPIRED)

            # Validate tasks
            self.assertFalse(Task.objects.exists())

            # Validate stops
            self.assertFalse(Stop.objects.exists())

    def test_invalidate_assignment__using_confirmed_assignment(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.patch(
                re.compile("http://order-api/packages/*"),
                status_code=status.HTTP_200_OK,
            )
            mocker.post(
                re.compile("http://communication-api/*"),
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

            invalidate_assignments(ASSIGNMENT_STATUS.EXPIRED, [assignment])

            # Validate assignment
            assignment.refresh_from_db()
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.EXPIRED)
            self.assertIsNotNone(assignment.confirmed_at)
            self.assertIsNotNone(assignment.confirmation_location)

            # Validate tasks
            self.assertFalse(Task.objects.exists())

            # Validate stops
            self.assertFalse(Stop.objects.exists())

    def test_invalidate_assignment__keeps_stops_for_other_assignments(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.patch(
                re.compile("http://order-api/packages/*"),
                status_code=status.HTTP_200_OK,
            )
            mocker.post(
                re.compile("http://communication-api/*"),
                status_code=status.HTTP_200_OK,
            )

            driver = self.create_driver()
            assignments = [
                self.create_assignment(driver=driver),
                self.create_assignment(driver=driver),
                self.create_confirmed_assignment(driver=driver),
            ]
            stops = [
                self.create_stop(driver=driver),
                self.create_stop(driver=driver),
                self.create_stop(driver=driver),
                self.create_stop(driver=driver),
            ]
            tasks = [
                self.create_task(
                    stop=stops[0], assignment=assignments[0], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[1], assignment=assignments[0], type=TASK_TYPE.DROP_OFF
                ),
                self.create_task(
                    stop=stops[1], assignment=assignments[1], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[2], assignment=assignments[1], type=TASK_TYPE.DROP_OFF
                ),
                self.create_task(
                    stop=stops[2], assignment=assignments[2], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[3], assignment=assignments[2], type=TASK_TYPE.DROP_OFF
                ),
            ]

            invalidate_assignments(ASSIGNMENT_STATUS.EXPIRED, [assignments[0]])

            # Validate assignment
            assignments[0].refresh_from_db()
            self.assertEqual(assignments[0].status, ASSIGNMENT_STATUS.EXPIRED)

            # Validate tasks
            self.assertFalse(Task.objects.filter(assignment=assignments[0]).exists())

            # Validate stops. Two stops were not deleted as they are associated with another task
            self.assertEqual(Stop.objects.filter(driver=driver).count(), 3)

    def test_invalidate_assignment__raises_error_if_updating_package_failed(
        self,
    ) -> None:
        assignment = self.create_confirmed_assignment()

        with self.assertRaisesMessage(
            Exception,
            "Unable to update package on order-api: {'errorCode': 'INVALID_REQUEST', 'errorMessage': 'Bad update package request'}",
        ):
            with requests_mock.Mocker() as mocker:
                mocker.patch(
                    re.compile("http://order-api/packages/*"),
                    status_code=status.HTTP_400_BAD_REQUEST,
                    json=camelize(
                        create_api_error(
                            ERROR_CODE.INVALID_REQUEST, "Bad update package request"
                        )
                    ),
                )

                invalidate_assignments(ASSIGNMENT_STATUS.EXPIRED, [assignment])

        # Should never update assignment
        assignment.refresh_from_db()
        self.assertEqual(assignment.status, ASSIGNMENT_STATUS.CONFIRMED)


@override_settings(
    VANOMA_ORDER_API_URL="http://order-api",
    VANOMA_COMMUNICATION_API_URL="http://communication-api",
)
class CreateAssingmentsTestCase(APITestCaseV1_0):
    def test_create_assignments__without_batching(self) -> None:
        package_ids = ["19b34722-f0af-42ba-bd02-0bc7131fae96"]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages/{package_ids[0]}",
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

            driver = self.create_driver_with_location()

            create_assignments(driver, ASSIGNMENT_TYPE.MANUAL, package_ids)

            # Validate assignment
            assignment = Assignment.objects.get(driver=driver)
            self.assertEqual(assignment.status, ASSIGNMENT_STATUS.PENDING)
            self.assertEqual(assignment.package_id, package_ids[0])

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

    def test_create_assignments__with_multiple_pickup_tasks_at_one_stop(self) -> None:
        """
        p1 and p3 are coming from the same stop while heading to different dropoff stops.
        The new batching model has multiple packages coming from the same pickup stop.
        """

        package_ids = [
            "19b34722-f0af-42ba-bd02-0bc7131fae96",  # p1
            "1e74abdf-9026-42ef-838b-31f6c9c8f2ad",  # p3
        ]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages/{package_ids[0]}",
                json=load_fixture("package1.json"),
            )
            mocker.get(
                f"http://order-api/packages/{package_ids[1]}",
                json=load_fixture("package3.json"),
            )
            mocker.get(  # Driver location to first pickup stop (p1from & p3from)
                re.compile(".*destinations=-1\.9434856%2C30\.0594882.*"),
                json=load_fixture("google-maps1.json"),
            )
            mocker.get(  # Pickup stop to first dropoff stop (p1to)
                re.compile(".*destinations=-1\.935355%2C30\.0444221.*"),
                json=load_fixture("google-maps1.json"),
            )
            mocker.get(  # Pickup to second dropoff stop (p3to)
                re.compile(".*destinations=-1\.9509743%2C30\.0599018.*"),
                json=load_fixture("google-maps2.json"),
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

            driver = self.create_driver_with_location()

            create_assignments(driver, ASSIGNMENT_TYPE.MANUAL, package_ids)

            # Validate assignments
            self.assertEqual(Assignment.objects.count(), 2)

            first_assignment = Assignment.objects.get(
                driver=driver, package_id=package_ids[0]
            )
            second_assignment = Assignment.objects.get(
                driver=driver, package_id=package_ids[1]
            )

            self.assertEqual(first_assignment.status, ASSIGNMENT_STATUS.PENDING)
            self.assertEqual(second_assignment.status, ASSIGNMENT_STATUS.PENDING)

            # Validate stops
            self.assertEqual(Stop.objects.count(), 3)

            first_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=first_assignment
            ).order_by("created_at")
            second_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=second_assignment
            ).order_by("created_at")

            self.assertEqual(first_assignment_stops[0], second_assignment_stops[0])

            self.assertEqual(first_assignment_stops[0].ranking, 0)
            self.assertEqual(first_assignment_stops[0].tasks.count(), 2)
            self.assertEqual(first_assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(first_assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(first_assignment_stops[1].ranking, 1)
            self.assertEqual(first_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(first_assignment_stops[1].latitude, -1.935355)
            self.assertEqual(first_assignment_stops[1].longitude, 30.0444221)

            self.assertEqual(second_assignment_stops[0].ranking, 0)
            self.assertEqual(second_assignment_stops[0].tasks.count(), 2)
            self.assertEqual(second_assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(second_assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(second_assignment_stops[1].ranking, 2)
            self.assertEqual(second_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(second_assignment_stops[1].latitude, -1.9509743)
            self.assertEqual(second_assignment_stops[1].longitude, 30.0599018)

            # Validate tasks
            self.assertEqual(Task.objects.count(), 4)

            first_stop_tasks = first_assignment_stops[0].tasks.all()
            second_stop_tasks = first_assignment_stops[1].tasks.all()
            third_stop_tasks = second_assignment_stops[1].tasks.all()

            self.assertEqual(len(first_stop_tasks), 2)
            self.assertEqual(first_stop_tasks[0].type, TASK_TYPE.PICK_UP)
            self.assertEqual(first_stop_tasks[1].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(second_stop_tasks), 1)
            self.assertEqual(second_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

            self.assertEqual(len(third_stop_tasks), 1)
            self.assertEqual(third_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

    def test_create_assignments__with_pickup_and_dropoff_tasks_at_one_stop(
        self,
    ) -> None:
        """
        The dropoff of p1 is the pickup of p2, but p1's pickup and p2's dropoff are different.
        The legacy batching model has packages pickup and dropoff stops form a linear sequence.
        """

        package_ids = [
            "19b34722-f0af-42ba-bd02-0bc7131fae96",  # p1
            "23876d54-78a8-4757-9090-bf4567ad12f2",  # p2
        ]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages/{package_ids[0]}",
                json=load_fixture("package1.json"),
            )
            mocker.get(
                f"http://order-api/packages/{package_ids[1]}",
                json=load_fixture("package2.json"),
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

            driver = self.create_driver_with_location()

            create_assignments(driver, ASSIGNMENT_TYPE.MANUAL, package_ids)

            # Validate assignments
            assignments = Assignment.objects.filter(driver=driver).order_by(
                "created_at"
            )

            self.assertEqual(assignments[0].status, ASSIGNMENT_STATUS.PENDING)
            self.assertEqual(assignments[0].package_id, package_ids[0])

            self.assertEqual(assignments[1].status, ASSIGNMENT_STATUS.PENDING)
            self.assertEqual(assignments[1].package_id, package_ids[1])

            # Validate stops
            self.assertEqual(Stop.objects.count(), 3)

            first_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=assignments[0]
            ).order_by("created_at")
            second_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=assignments[1]
            ).order_by("created_at")

            self.assertEqual(first_assignment_stops[0].ranking, 0)
            self.assertEqual(first_assignment_stops[0].tasks.count(), 1)
            self.assertEqual(first_assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(first_assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(first_assignment_stops[1].ranking, 1)
            self.assertEqual(first_assignment_stops[1].tasks.count(), 2)
            self.assertEqual(first_assignment_stops[1].latitude, -1.935355)
            self.assertEqual(first_assignment_stops[1].longitude, 30.0444221)

            self.assertEqual(second_assignment_stops[0].ranking, 1)
            self.assertEqual(second_assignment_stops[0].tasks.count(), 2)
            self.assertEqual(second_assignment_stops[0].latitude, -1.935355)
            self.assertEqual(second_assignment_stops[0].longitude, 30.0444221)

            self.assertEqual(second_assignment_stops[1].ranking, 2)
            self.assertEqual(second_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(second_assignment_stops[1].latitude, -1.9509743)
            self.assertEqual(second_assignment_stops[1].longitude, 30.0599018)

            # Validate tasks
            self.assertEqual(Task.objects.count(), 4)

            first_stop_tasks = first_assignment_stops[0].tasks.all()
            second_stop_tasks = first_assignment_stops[1].tasks.all()
            third_stop_tasks = second_assignment_stops[1].tasks.all()

            self.assertEqual(len(first_stop_tasks), 1)
            self.assertEqual(first_stop_tasks[0].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(second_stop_tasks), 2)
            self.assertEqual(second_stop_tasks[0].type, TASK_TYPE.DROP_OFF)
            self.assertEqual(second_stop_tasks[1].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(third_stop_tasks), 1)
            self.assertEqual(third_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

    def test_create_assignments__with_stops_having_circular_path(self) -> None:
        """
        The dropoff of p1 is the pickup of p4 and the dropoff of p4 is the pickup of p1.
        """

        package_ids = [
            "19b34722-f0af-42ba-bd02-0bc7131fae96",  # p1
            "02e6a3ab-c84f-4f5a-b377-41fbc476df9d",  # p4
        ]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages/{package_ids[0]}",
                json=load_fixture("package1.json"),
            )
            mocker.get(
                f"http://order-api/packages/{package_ids[1]}",
                json=load_fixture("package4.json"),
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

            driver = self.create_driver_with_location()

            create_assignments(driver, ASSIGNMENT_TYPE.MANUAL, package_ids)

            # Validate assignments
            assignments = Assignment.objects.filter(driver=driver).order_by(
                "created_at"
            )

            self.assertEqual(assignments[0].status, ASSIGNMENT_STATUS.PENDING)
            self.assertEqual(assignments[0].package_id, package_ids[0])

            self.assertEqual(assignments[1].status, ASSIGNMENT_STATUS.PENDING)
            self.assertEqual(assignments[1].package_id, package_ids[1])

            # Validate stops
            self.assertEqual(Stop.objects.count(), 3)

            first_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=assignments[0]
            ).order_by("created_at")
            second_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=assignments[1]
            ).order_by("created_at")

            self.assertEqual(first_assignment_stops[0].ranking, 0)
            self.assertEqual(first_assignment_stops[0].tasks.count(), 1)
            self.assertEqual(first_assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(first_assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(first_assignment_stops[1].ranking, 1)
            self.assertEqual(first_assignment_stops[1].tasks.count(), 2)
            self.assertEqual(first_assignment_stops[1].latitude, -1.935355)
            self.assertEqual(first_assignment_stops[1].longitude, 30.0444221)

            self.assertEqual(second_assignment_stops[0].ranking, 1)
            self.assertEqual(second_assignment_stops[0].tasks.count(), 2)
            self.assertEqual(second_assignment_stops[0].latitude, -1.935355)
            self.assertEqual(second_assignment_stops[0].longitude, 30.0444221)

            self.assertEqual(second_assignment_stops[1].ranking, 2)
            self.assertEqual(second_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(second_assignment_stops[1].latitude, -1.9434856)
            self.assertEqual(second_assignment_stops[1].longitude, 30.0594882)

            # Validate tasks
            self.assertEqual(Task.objects.count(), 4)

            first_stop_tasks = first_assignment_stops[0].tasks.all()
            second_stop_tasks = first_assignment_stops[1].tasks.all()
            third_stop_tasks = second_assignment_stops[1].tasks.all()

            self.assertEqual(len(first_stop_tasks), 1)
            self.assertEqual(first_stop_tasks[0].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(second_stop_tasks), 2)
            self.assertEqual(second_stop_tasks[0].type, TASK_TYPE.DROP_OFF)
            self.assertEqual(second_stop_tasks[1].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(third_stop_tasks), 1)
            self.assertEqual(third_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

    def test_create_assignments__skips_using_completed_pickup_stop_for_multiple_pickup_tasks_at_one_stop(
        self,
    ) -> None:
        """
        p1 and p3 have the same pickup stop; p1 was assigned earlier and it was picked up.
        A new assignment for p3 should create new pickup and dropoff stops and not reuse p1's
        pickup stop.
        """

        package_ids = ["19b34722-f0af-42ba-bd02-0bc7131fae96"]  # p1
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages/{package_ids[0]}",
                json=load_fixture("package1.json"),
            )
            mocker.get(  # Driver locatin to first pickup stop (p1from & p3from)
                re.compile(".*destinations=-1\.9434856%2C30\.0594882.*"),
                json=load_fixture("google-maps1.json"),
            )
            mocker.get(  # Last pickup stop to first dropoff stop (p1to)
                re.compile(".*destinations=-1\.935355%2C30\.0444221.*"),
                json=load_fixture("google-maps1.json"),
            )
            mocker.get(  # Last pickup stop to second dropoff stop (p3to)
                re.compile(".*destinations=-1\.9509743%2C30\.0599018.*"),
                json=load_fixture("google-maps2.json"),
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

            driver = self.create_driver_with_location()
            existing_assignment = (
                self.create_confirmed_assignment(  # Existing assignment for p3
                    driver=driver, package_id="1e74abdf-9026-42ef-838b-31f6c9c8f2ad"
                )
            )
            existing_stops = [
                self.create_stop(
                    driver=driver,
                    ranking=0,
                    latitude=-1.9434856,
                    longitude=30.0594882,
                    completed_at=timezone.now(),
                ),
                self.create_stop(
                    driver=driver,
                    ranking=1,
                    latitude=-1.9509743,
                    longitude=30.0599018,
                ),
            ]
            existing_tasks = [
                self.create_task(
                    type=TASK_TYPE.PICK_UP,
                    stop=existing_stops[0],
                    assignment=existing_assignment,
                    completed_at=timezone.now(),
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF,
                    stop=existing_stops[1],
                    assignment=existing_assignment,
                ),
            ]

            create_assignments(driver, ASSIGNMENT_TYPE.MANUAL, package_ids)

            # Validate assignment
            new_assignment = Assignment.objects.get(package_id=package_ids[0])
            self.assertEqual(new_assignment.status, ASSIGNMENT_STATUS.PENDING)

            # Validate stops
            self.assertEqual(Stop.objects.count(), 4)

            existing_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=existing_assignment
            ).order_by("created_at")
            new_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=new_assignment
            ).order_by("created_at")

            self.assertEqual(existing_assignment_stops[0].ranking, 0)
            self.assertEqual(existing_assignment_stops[0].tasks.count(), 1)
            self.assertEqual(existing_assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(existing_assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(existing_assignment_stops[1].ranking, 1)
            self.assertEqual(existing_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(existing_assignment_stops[1].latitude, -1.9509743)
            self.assertEqual(existing_assignment_stops[1].longitude, 30.0599018)

            self.assertEqual(new_assignment_stops[0].ranking, 2)
            self.assertEqual(new_assignment_stops[0].tasks.count(), 1)
            self.assertEqual(new_assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(new_assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(new_assignment_stops[1].ranking, 3)
            self.assertEqual(new_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(new_assignment_stops[1].latitude, -1.935355)
            self.assertEqual(new_assignment_stops[1].longitude, 30.0444221)

            # Validate tasks
            self.assertEqual(Task.objects.count(), 4)

            first_stop_tasks = existing_assignment_stops[0].tasks.all()
            second_stop_tasks = existing_assignment_stops[1].tasks.all()
            third_stop_tasks = new_assignment_stops[0].tasks.all()
            fourth_stop_tasks = new_assignment_stops[1].tasks.all()

            self.assertEqual(len(first_stop_tasks), 1)
            self.assertEqual(first_stop_tasks[0].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(second_stop_tasks), 1)
            self.assertEqual(second_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

            self.assertEqual(len(third_stop_tasks), 1)
            self.assertEqual(third_stop_tasks[0].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(fourth_stop_tasks), 1)
            self.assertEqual(fourth_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

    def test_create_assignments__skips_using_completed_pickup_stop_for_pickup_and_dropoff_tasks_at_one_stop(
        self,
    ) -> None:
        """
        The dropoff of p1 is the pickup of p2; p1 was assigned earlier and it was both picked
        up and dropped off. A new assignment for p2 should create new pickup and dropoff stops
        and not reuse p1's pickup stop.
        """

        package_ids = ["23876d54-78a8-4757-9090-bf4567ad12f2"]  # p2
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages/{package_ids[0]}",
                json=load_fixture("package2.json"),
            )
            mocker.get(  # Driver locatin to pickup stop
                re.compile(".*destinations=-1\.935355%2C30\.0444221.*"),
                json=load_fixture("google-maps1.json"),
            )
            mocker.get(  # Last pickup stop to dropoff stop
                re.compile(".*destinations=-1\.9509743%2C30\.0599018.*"),
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

            driver = self.create_driver_with_location()
            existing_assignment = (
                self.create_confirmed_assignment(  # Existing assignment for p1
                    driver=driver, package_id="19b34722-f0af-42ba-bd02-0bc7131fae96"
                )
            )
            existing_stops = [
                self.create_stop(
                    driver=driver,
                    ranking=0,
                    latitude=-1.9434856,
                    longitude=30.0594882,
                    completed_at=timezone.now(),
                ),
                self.create_stop(
                    driver=driver,
                    ranking=1,
                    latitude=-1.935355,
                    longitude=30.0444221,
                    completed_at=timezone.now(),
                ),
            ]
            existing_tasks = [
                self.create_task(
                    type=TASK_TYPE.PICK_UP,
                    stop=existing_stops[0],
                    assignment=existing_assignment,
                    completed_at=timezone.now(),
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF,
                    stop=existing_stops[1],
                    assignment=existing_assignment,
                    completed_at=timezone.now(),
                ),
            ]

            create_assignments(driver, ASSIGNMENT_TYPE.MANUAL, package_ids)

            # Validate assignment
            new_assignment = Assignment.objects.get(package_id=package_ids[0])
            self.assertEqual(new_assignment.status, ASSIGNMENT_STATUS.PENDING)

            # Validate stops
            self.assertEqual(Stop.objects.count(), 4)

            existing_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=existing_assignment
            ).order_by("created_at")
            new_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=new_assignment
            ).order_by("created_at")

            self.assertEqual(existing_assignment_stops[0].ranking, 0)
            self.assertEqual(existing_assignment_stops[0].tasks.count(), 1)
            self.assertEqual(existing_assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(existing_assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(existing_assignment_stops[1].ranking, 1)
            self.assertEqual(existing_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(existing_assignment_stops[1].latitude, -1.935355)
            self.assertEqual(existing_assignment_stops[1].longitude, 30.0444221)

            self.assertEqual(new_assignment_stops[0].ranking, 2)
            self.assertEqual(new_assignment_stops[0].tasks.count(), 1)
            self.assertEqual(new_assignment_stops[0].latitude, -1.935355)
            self.assertEqual(new_assignment_stops[0].longitude, 30.0444221)

            self.assertEqual(new_assignment_stops[1].ranking, 3)
            self.assertEqual(new_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(new_assignment_stops[1].latitude, -1.9509743)
            self.assertEqual(new_assignment_stops[1].longitude, 30.0599018)

            # Validate tasks
            self.assertEqual(Task.objects.count(), 4)

            first_stop_tasks = existing_assignment_stops[0].tasks.all()
            second_stop_tasks = existing_assignment_stops[1].tasks.all()
            third_stop_tasks = new_assignment_stops[0].tasks.all()
            fourth_stop_tasks = new_assignment_stops[1].tasks.all()

            self.assertEqual(len(first_stop_tasks), 1)
            self.assertEqual(first_stop_tasks[0].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(second_stop_tasks), 1)
            self.assertEqual(second_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

            self.assertEqual(len(third_stop_tasks), 1)
            self.assertEqual(third_stop_tasks[0].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(fourth_stop_tasks), 1)
            self.assertEqual(fourth_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

    def test_create_assignments__reuses_incomplete_pickup_stop_for_pickup_and_dropoff_tasks_at_one_stop(
        self,
    ) -> None:
        """
        The dropoff of p1 is the pickup of p2; p1 was assigned earlier and it was picked up but not dropped
        off. A new assignment for p2 should reuse p1's dropoff stop as the pickup stop instead of creating a new one.
        """

        package_ids = ["23876d54-78a8-4757-9090-bf4567ad12f2"]  # p2
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages/{package_ids[0]}",
                json=load_fixture("package2.json"),
            )
            mocker.get(  # Driver locatin to pickup stop
                re.compile(".*destinations=-1\.935355%2C30\.0444221.*"),
                json=load_fixture("google-maps1.json"),
            )
            mocker.get(  # Last pickup stop to dropoff stop
                re.compile(".*destinations=-1\.9509743%2C30\.0599018.*"),
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

            driver = self.create_driver_with_location()
            existing_assignment = (
                self.create_confirmed_assignment(  # Existing assignment for p1
                    driver=driver, package_id="19b34722-f0af-42ba-bd02-0bc7131fae96"
                )
            )
            existing_stops = [
                self.create_stop(
                    driver=driver,
                    ranking=0,
                    latitude=-1.9434856,
                    longitude=30.0594882,
                    completed_at=timezone.now(),
                ),
                self.create_stop(
                    driver=driver,
                    ranking=1,
                    latitude=-1.935355,
                    longitude=30.0444221,
                ),
            ]
            existing_tasks = [
                self.create_task(
                    type=TASK_TYPE.PICK_UP,
                    stop=existing_stops[0],
                    assignment=existing_assignment,
                    completed_at=timezone.now(),
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF,
                    stop=existing_stops[1],
                    assignment=existing_assignment,
                ),
            ]

            create_assignments(driver, ASSIGNMENT_TYPE.MANUAL, package_ids)

            # Validate assignment
            new_assignment = Assignment.objects.get(package_id=package_ids[0])
            self.assertEqual(new_assignment.status, ASSIGNMENT_STATUS.PENDING)

            # Validate stops
            self.assertEqual(Stop.objects.count(), 3)

            existing_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=existing_assignment
            ).order_by("created_at")
            new_assignment_stops = Stop.objects.filter(
                driver=driver, tasks__assignment=new_assignment
            ).order_by("created_at")

            self.assertEqual(existing_assignment_stops[1], new_assignment_stops[0])

            self.assertEqual(existing_assignment_stops[0].ranking, 0)
            self.assertEqual(existing_assignment_stops[0].tasks.count(), 1)
            self.assertEqual(existing_assignment_stops[0].latitude, -1.9434856)
            self.assertEqual(existing_assignment_stops[0].longitude, 30.0594882)

            self.assertEqual(existing_assignment_stops[1].ranking, 1)
            self.assertEqual(existing_assignment_stops[1].tasks.count(), 2)
            self.assertEqual(existing_assignment_stops[1].latitude, -1.935355)
            self.assertEqual(existing_assignment_stops[1].longitude, 30.0444221)

            self.assertEqual(new_assignment_stops[0].ranking, 1)
            self.assertEqual(new_assignment_stops[0].tasks.count(), 2)
            self.assertEqual(new_assignment_stops[0].latitude, -1.935355)
            self.assertEqual(new_assignment_stops[0].longitude, 30.0444221)

            self.assertEqual(new_assignment_stops[1].ranking, 2)
            self.assertEqual(new_assignment_stops[1].tasks.count(), 1)
            self.assertEqual(new_assignment_stops[1].latitude, -1.9509743)
            self.assertEqual(new_assignment_stops[1].longitude, 30.0599018)

            # Validate tasks
            self.assertEqual(Task.objects.count(), 4)

            first_stop_tasks = existing_assignment_stops[0].tasks.all()
            second_stop_tasks = existing_assignment_stops[1].tasks.all()
            third_stop_tasks = new_assignment_stops[1].tasks.all()

            self.assertEqual(len(first_stop_tasks), 1)
            self.assertEqual(first_stop_tasks[0].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(second_stop_tasks), 2)
            self.assertEqual(second_stop_tasks[0].type, TASK_TYPE.DROP_OFF)
            self.assertEqual(second_stop_tasks[1].type, TASK_TYPE.PICK_UP)

            self.assertEqual(len(third_stop_tasks), 1)
            self.assertEqual(third_stop_tasks[0].type, TASK_TYPE.DROP_OFF)

    # TODO: Add this test case
    def test_create_assignments__combine_multiple_pickup_tasks_at_one_stop_with_pickup_and_dropoff_tasks_at_one_stop(
        self,
    ) -> None:
        """
        For p1 (A -> B), p2 (A -> C), p3 -> (C -> D), we need to sort stops as A -> B -> C -> D
        """

    def test_create_assignments__raises_error_if_updating_package_failed(
        self,
    ) -> None:
        with self.assertRaisesMessage(
            Exception,
            "Unable to update package on order-api: {'errorCode': 'INVALID_REQUEST', 'errorMessage': 'Bad update package request'}",
        ):
            package_ids = ["19b34722-f0af-42ba-bd02-0bc7131fae96"]
            with requests_mock.Mocker() as mocker:
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
                    status_code=status.HTTP_400_BAD_REQUEST,
                    json=camelize(
                        create_api_error(
                            ERROR_CODE.INVALID_REQUEST, "Bad update package request"
                        )
                    ),
                )

                driver = self.create_driver_with_location(is_available=True)

                create_assignments(driver, ASSIGNMENT_TYPE.MANUAL, package_ids)

        # Should never create assignment, stops and tasks
        self.assertEqual(Assignment.objects.count(), 0)
        self.assertEqual(Stop.objects.count(), 0)
        self.assertEqual(Task.objects.count(), 0)


class FindAssignableDriverTestCase(APITestCaseV1_0):
    def test_find_assignable_driver__sorts_drivers_by_estimated_duration_first(
        self,
    ) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.get(  # Response for 1st driver's estimated duration
                re.compile(".*origins=-1\.9353231%2C30\.0692974.*"),
                json=load_fixture("google-maps1.json"),
            )
            mocker.get(  # Response for 2nd driver's estimated duration
                re.compile(".*origins=-1\.9899248%2C30\.0418526.*"),
                json=load_fixture("google-maps2.json"),
            )

            drivers = [
                self.create_driver(is_available=True),
                self.create_driver(is_available=True),
            ]
            locations = [
                self.create_location(driver=drivers[0]),
                self.create_location(
                    driver=drivers[1], latitude="-1.9899248", longitude="30.0418526"
                ),
            ]
            assignments = [
                self.create_assignment(  # 1st driver has completed an assignment already
                    driver=drivers[0], status=ASSIGNMENT_STATUS.COMPLETED
                )
            ]
            package = underscoreize(load_fixture("package1.json"))

            driver = find_assignable_driver(package)

            self.assertEqual(driver.driver_id, str(drivers[0].driver_id))  # type: ignore

    def test_find_assignable_driver__sorts_drivers_by_last_assignment_completion_second(
        self,
    ) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.get(  # Returns same estimated duration for both drivers
                "https://maps.googleapis.com/maps/api/distancematrix/json",
                json=load_fixture("google-maps1.json"),
            )

            drivers = [
                self.create_driver_with_location(is_available=True),
                self.create_driver_with_location(is_available=True),
            ]
            assignments = [
                self.create_assignment(  # 2nd driver has completed an assignment earlier than 1st driver
                    driver=drivers[1], status=ASSIGNMENT_STATUS.COMPLETED
                ),
                self.create_assignment(
                    driver=drivers[0], status=ASSIGNMENT_STATUS.COMPLETED
                ),
            ]
            package = underscoreize(load_fixture("package1.json"))

            driver = find_assignable_driver(package)

            self.assertEqual(driver.driver_id, str(drivers[1].driver_id))  # type: ignore

    def test_find_assignable_driver__uses_active_drivers_only(self) -> None:
        drivers = [
            self.create_driver(is_available=True, status=item[0])
            for item in DRIVER_STATUS.ALL
            if item[0] != DRIVER_STATUS.ACTIVE
        ]
        package = load_fixture("package1.json")

        driver = find_assignable_driver(package)

        self.assertIsNone(driver)

    def test_find_assignable_driver__excludes_assigned_drivers(self) -> None:
        drivers = [
            self.create_driver(is_available=True),
            self.create_driver(is_available=True),
        ]
        assignments = [
            self.create_assignment(driver=drivers[0]),  # Pending assignment
            self.create_confirmed_assignment(driver=drivers[1]),  # Confirmed assignment
        ]
        package = load_fixture("package1.json")

        driver = find_assignable_driver(package)

        self.assertIsNone(driver)

    def test_find_assignable_driver__excludes_unavailable_drivers(self) -> None:
        drivers = [
            self.create_driver(is_available=False),
            self.create_driver(is_available=False),
        ]
        package = load_fixture("package1.json")

        driver = find_assignable_driver(package)

        self.assertIsNone(driver)
