from typing import Any, Dict, List
from uuid import uuid4
from django.utils import timezone
from django.core.files.base import ContentFile
from rest_framework.test import APITestCase
from vanoma_api_utils import dates
from vanoma_api_utils.tests import faker, random_phone_number
from vanoma_api_utils.django.tests import load_fixture
from vanoma_api_utils.rest_framework import tests
from delivery_api.drivers.models import Driver, Location
from delivery_api.drivers.utils.constants import DRIVER_STATUS
from delivery_api.deliveries.models import Assignment, Delay, Stop, Task
from delivery_api.deliveries.utils.constants import (
    ASSIGNMENT_STATUS,
    ASSIGNMENT_TYPE,
    DELAY_TYPE,
    DELAY_STATUS,
    TASK_TYPE,
)


class ModelsMixin:
    def create_driver(self, **kwargs: Any) -> Driver:
        if "first_name" not in kwargs:
            kwargs["first_name"] = faker.first_name()

        if "last_name" not in kwargs:
            kwargs["last_name"] = faker.first_name()

        if "phone_number" not in kwargs:
            kwargs["phone_number"] = random_phone_number()

        if "second_phone_number" not in kwargs:
            kwargs["second_phone_number"] = random_phone_number()

        if "status" not in kwargs:
            kwargs["status"] = DRIVER_STATUS.ACTIVE

        return Driver.objects.create(**kwargs)

    def create_driver_with_location(self, **kwargs: Any) -> Driver:
        driver = self.create_driver(**kwargs)
        self.create_location(driver=driver)
        return driver

    def create_location(self, **kwargs: Any) -> Location:
        if "driver" not in kwargs:
            kwargs["driver"] = self.create_driver()

        if "latitude" not in kwargs:
            # Using static values instead of faker.latitude() to avoid formatting
            # differences in rendered values.
            kwargs["latitude"] = "-1.93532310"

        if "longitude" not in kwargs:
            # Using static values instead of faker.latitude() to avoid formatting
            # differences in rendered values.
            kwargs["longitude"] = "30.06929740"

        return Location.objects.create(**kwargs)

    def create_delay(self, **kwargs: Any) -> Delay:
        if "driver" not in kwargs:
            kwargs["driver"] = self.create_driver()

        if "type" not in kwargs:
            kwargs["type"] = DELAY_TYPE.STOP
            kwargs["stop"] = self.create_stop(driver=kwargs["driver"])

        if "status" not in kwargs:
            kwargs["status"] = DELAY_STATUS.PENDING

        if "justification" not in kwargs:
            # Unfortunately we must set a file to this field to avoid the render method
            # below from throwing "file not sett" exception.
            kwargs["justification"] = ContentFile("dummy contents", name="dummy name")

        return Delay.objects.create(**kwargs)

    def create_stop(self, **kwargs: Any) -> Stop:
        if "driver" not in kwargs:
            kwargs["driver"] = self.create_driver()

        if "ranking" not in kwargs:
            kwargs["ranking"] = faker.pyint()

        if "latitude" not in kwargs:
            kwargs["latitude"] = float(faker.latitude())

        if "longitude" not in kwargs:
            kwargs["longitude"] = float(faker.longitude())

        return Stop.objects.create(**kwargs)

    def create_task(self, **kwargs: Any) -> Task:
        if "stop" not in kwargs:
            kwargs["stop"] = self.create_stop()

        if "assignment" not in kwargs:
            kwargs["assignment"] = self.create_assignment()

        if "type" not in kwargs:
            kwargs["type"] = TASK_TYPE.PICK_UP

        return Task.objects.create(**kwargs)

    def create_assignment(self, **kwargs: Any) -> Assignment:
        if "driver" not in kwargs:
            kwargs["driver"] = self.create_driver()

        if "package_id" not in kwargs:
            kwargs["package_id"] = str(uuid4())

        if "type" not in kwargs:
            kwargs["type"] = ASSIGNMENT_TYPE.MANUAL

        if "status" not in kwargs:
            kwargs["status"] = ASSIGNMENT_STATUS.PENDING

        return Assignment.objects.create(**kwargs)

    def create_confirmed_assignment(self, **kwargs: Any) -> Assignment:
        if "driver" not in kwargs:
            kwargs["driver"] = self.create_driver()

        if "confirmed_at" not in kwargs:
            kwargs["confirmed_at"] = timezone.now()

        if "confirmation_location" not in kwargs:
            kwargs["confirmation_location"] = self.create_location(
                driver=kwargs["driver"]
            )

        return self.create_assignment(status=ASSIGNMENT_STATUS.CONFIRMED, **kwargs)


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

    def render_driver(self, driver: Driver) -> Dict[str, Any]:
        latest_location = (
            self.render_location(driver.latest_location)
            if driver.latest_location
            else None
        )
        assignment_count = driver.assignments.filter(
            status=ASSIGNMENT_STATUS.CONFIRMED
        ).count()

        return {
            "driverId": str(driver.driver_id),
            "firstName": driver.first_name,
            "lastName": driver.last_name,
            "phoneNumber": driver.phone_number,
            "secondPhoneNumber": driver.second_phone_number,
            "status": driver.status,
            "isAvailable": driver.is_available,
            "isFullTime": driver.is_full_time,
            "isAssignable": driver.is_assignable,
            "latestLocation": latest_location,
            "assignmentCount": assignment_count,
            "createdAt": dates.format_datetime(driver.created_at),
            "updatedAt": dates.format_datetime(driver.updated_at),
        }

    def render_location(self, location: Location) -> Dict[str, Any]:
        return {
            "locationId": str(location.location_id),
            "isAssigned": location.is_assigned,
            "latitude": float(location.latitude),
            "longitude": float(location.longitude),
            "batteryLevel": location.battery_level,
            "isGpsEnabled": location.is_gps_enabled,
            "isLocationServiceEnabled": location.is_location_service_enabled,
            "locationAccessStatus": location.location_access_status,
            "createdAt": dates.format_datetime(location.created_at),
            "updatedAt": dates.format_datetime(location.updated_at),
        }

    def render_delay(self, delay: Delay) -> Dict[str, Any]:
        stop = self._render_delay_stop(delay.stop) if delay.stop else None
        assignment = (
            self.render_assignment(delay.assignment) if delay.assignment else None
        )

        return {
            "delayId": str(delay.delay_id),
            "driver": self.render_driver(delay.driver),
            "stop": stop,
            "assignment": assignment,
            "type": delay.type,
            "status": delay.status,
            "justification": f"http://testserver{delay.justification.url}",
            "createdAt": dates.format_datetime(delay.created_at),
            "updatedAt": dates.format_datetime(delay.updated_at),
        }

    def render_current_assignment(
        self, assignment: Assignment, package_fixture: str
    ) -> Dict[str, Any]:
        return {
            **self.render_assignment(assignment, True),
            "package": load_fixture(package_fixture),
        }

    def render_assignment(
        self, assignment: Assignment, is_current: bool = False
    ) -> Dict[str, Any]:
        confirmed_at = (
            dates.format_datetime(assignment.confirmed_at)
            if assignment.confirmed_at
            else None
        )

        rendered_assignment = {
            "assignmentId": str(assignment.assignment_id),
            "driver": self.render_driver(assignment.driver),
            "type": assignment.type,
            "status": assignment.status,
            "confirmedAt": confirmed_at,
            "createdAt": dates.format_datetime(assignment.created_at),
            "updatedAt": dates.format_datetime(assignment.updated_at),
        }

        if is_current:
            return rendered_assignment

        confirmation_location = (
            self.render_location(assignment.confirmation_location)
            if assignment.confirmation_location
            else None
        )

        return {
            **rendered_assignment,
            "packageId": assignment.package_id,
            "confirmationLocation": confirmation_location,
            "tasks": [
                self._render_task(t) for t in assignment.tasks.order_by("stop__ranking")
            ],
        }

    def render_current_stop(self, stop: Stop, package_fixture: str) -> Dict[str, Any]:
        current_tasks = [
            self._render_current_task(task, package_fixture)
            for task in stop.tasks.filter(
                assignment__status=ASSIGNMENT_STATUS.CONFIRMED
            ).all()
        ]

        return {
            **self._render_stop(stop),
            "currentTasks": current_tasks,
        }

    def _render_delay_stop(self, stop: Stop) -> Dict[str, Any]:
        depart_by = dates.format_datetime(stop.depart_by) if stop.depart_by else None
        arrive_by = dates.format_datetime(stop.arrive_by) if stop.arrive_by else None
        departed_at = (
            dates.format_datetime(stop.departed_at) if stop.departed_at else None
        )
        arrived_at = dates.format_datetime(stop.arrived_at) if stop.arrived_at else None

        return {
            "stopId": str(stop.stop_id),
            "ranking": stop.ranking,
            "latitude": stop.latitude,
            "longitude": stop.longitude,
            "departBy": depart_by,
            "arriveBy": arrive_by,
            "departedAt": departed_at,
            "arrivedAt": arrived_at,
            "createdAt": dates.format_datetime(stop.created_at),
            "updatedAt": dates.format_datetime(stop.updated_at),
        }

    def _render_current_task(self, task: Task, package_fixture: str) -> Dict[str, Any]:
        return {
            **self._render_task(task, True),
            "package": load_fixture(package_fixture),
        }

    def _render_task(self, task: Task, is_current: bool = False) -> Dict[str, Any]:
        completed_at = (
            dates.format_datetime(task.completed_at) if task.completed_at else None
        )

        rendered_task = {
            "taskId": str(task.task_id),
            "type": task.type,
            "completedAt": completed_at,
            "createdAt": dates.format_datetime(task.created_at),
            "updatedAt": dates.format_datetime(task.updated_at),
        }

        if is_current:
            return rendered_task

        return {
            **rendered_task,
            "stop": self._render_stop(task.stop),
        }

    def _render_stop(self, stop: Stop) -> Dict[str, Any]:
        completed_at = (
            dates.format_datetime(stop.completed_at) if stop.completed_at else None
        )
        depart_by = dates.format_datetime(stop.depart_by) if stop.depart_by else None
        arrive_by = dates.format_datetime(stop.arrive_by) if stop.arrive_by else None
        departed_at = (
            dates.format_datetime(stop.departed_at) if stop.departed_at else None
        )
        arrived_at = dates.format_datetime(stop.arrived_at) if stop.arrived_at else None

        return {
            "stopId": str(stop.stop_id),
            "ranking": stop.ranking,
            "latitude": stop.latitude,
            "longitude": stop.longitude,
            "completedAt": completed_at,
            "departBy": depart_by,
            "arriveBy": arrive_by,
            "departedAt": departed_at,
            "arrivedAt": arrived_at,
            "createdAt": dates.format_datetime(stop.created_at),
            "updatedAt": dates.format_datetime(stop.updated_at),
        }
