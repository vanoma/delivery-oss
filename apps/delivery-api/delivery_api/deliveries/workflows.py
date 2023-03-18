from functools import cmp_to_key
from typing import Any, Dict, List, Tuple, Union
from datetime import datetime, timedelta
from collections import defaultdict
from django.db import transaction
from django.utils import timezone
from django.utils.translation import gettext_lazy as _
from django.db.models import Q
from django.db.models.query import QuerySet
from rest_framework import exceptions
from delivery_api import settings
from delivery_api.drivers.utils import communication_api
from delivery_api.deliveries.utils.functions import haversine_distance
from delivery_api.drivers.models import Driver, Location
from delivery_api.drivers.utils.constants import DRIVER_STATUS
from delivery_api.huey import mini_huey
from .utils.constants import (
    ASSIGNMENT_STATUS,
    DELAY_STATUS,
    DELAY_TYPE,
    TASK_TYPE,
)
from .models import Assignment, Delay, Stop, Task
from .utils import google, order_api


def _get_existing_stop(driver: Driver, address: Dict[str, Any]) -> Union[Stop, None]:
    existing_stops = Stop.objects.filter(
        Q(tasks__assignment__status=ASSIGNMENT_STATUS.PENDING)
        | Q(tasks__assignment__status=ASSIGNMENT_STATUS.CONFIRMED),
        driver=driver,
    )

    for stop in existing_stops.all():
        distance = haversine_distance(
            stop.latitude,
            stop.longitude,
            address["latitude"],
            address["longitude"],
        )
        # The address is concered part of the stop if it's within the 25 meters
        if distance <= 25:
            return stop

    return None


def _are_stops_circular(initial_stop: Stop, final_stop: Stop) -> bool:
    visited_stops = set()
    unvisited_stops = [initial_stop]

    while len(unvisited_stops) > 0:
        current_stop = unvisited_stops.pop(0)
        visited_stops.add(current_stop)

        if current_stop == final_stop:
            return True

        for task in current_stop.tasks.all():
            for t in task.assignment.tasks.all():
                if not t.stop in visited_stops:
                    unvisited_stops.append(t.stop)

    return False


def _get_or_create_stops(
    driver: Driver, pickup_address: Dict[str, Any], dropoff_address: Dict[str, Any]
) -> Tuple[Stop, Stop]:
    existing_pickup_stop = _get_existing_stop(driver, pickup_address)
    existing_dropoff_stop = _get_existing_stop(driver, dropoff_address)

    # If there's an existing pickup stop with incomplete tasks, we can reuse it instead of creating one.
    # For an existing dropoff stop, however, we must ensure that the reusing the existing stop will not
    # result in a circular path so that if we have packages p1 (A -> B) and p2 (B -> A), the resulting
    # stops should look like A(p1) -> B(p1,p2) -> A(p2)
    if existing_pickup_stop and existing_pickup_stop.completed_at is None:
        if existing_dropoff_stop and not _are_stops_circular(
            existing_dropoff_stop, existing_pickup_stop
        ):
            return (existing_pickup_stop, existing_dropoff_stop)

        return (
            existing_pickup_stop,
            Stop.objects.create(
                driver=driver,
                latitude=dropoff_address["latitude"],
                longitude=dropoff_address["longitude"],
            ),
        )

    # If there's no existing pickup stop or tasks at the pickup stop are all completed, we have to
    # create new stops. For completed tasks case, we have to return new stops so that we can always
    # prioritize finishing a started assignment. There may be a room for improvement there where we
    # can still return either stop even if the tasks were completed but the driver hasn't departed
    # the pickup stop yet; this would be very useful for  example after a driver picks up a package
    # and we get another order from the same pickup location right after the pickup. In such cases,
    # it makes sense to return the existing pickup stop instead of creating a new one.
    return (
        Stop.objects.create(
            driver=driver,
            latitude=pickup_address["latitude"],
            longitude=pickup_address["longitude"],
        ),
        Stop.objects.create(
            driver=driver,
            latitude=dropoff_address["latitude"],
            longitude=dropoff_address["longitude"],
        ),
    )


def _get_estimated_duration_between_stops(
    pickup_stop: Dict[str, Any], dropoff_stop: Dict[str, Any], depart_by: datetime
) -> int:
    return google.get_estimated_duration(
        google.Coordinates(pickup_stop["latitude"], pickup_stop["longitude"]),
        google.Coordinates(dropoff_stop["latitude"], dropoff_stop["longitude"]),
        depart_by,
    )


def _get_estimated_duration_from_driver(
    driver: Driver, stop_or_address: Dict[str, Any], depart_by: datetime
) -> int:
    if not driver.latest_location:
        raise exceptions.ValidationError(_("Driver has no latest location."))

    return google.get_estimated_duration(
        google.Coordinates(
            driver.latest_location.latitude, driver.latest_location.longitude
        ),
        google.Coordinates(stop_or_address["latitude"], stop_or_address["longitude"]),
        depart_by,
    )


def _get_sortable_stops(
    driver: Driver, excluded_stops: Dict[str, Any]
) -> Tuple[List[str], Dict[str, Any]]:
    """
    Construct and returns a tuple containing the following data structures:
    all_stop_details = {
        stopId1: { stopId, addressId, ..., tasks: [task1], assignments: [assignment1]},
        stopId2: { stopId, addressId, ..., tasks: [task1, task2], assignments: [assignment1, assignment2]},
        stopId3: { stopId, addressId, ..., tasks: [task1], assignments: [assignment2]},
    }
    sortable_stop_ids = [stopId1, stopId2, stopId2, stopId3]
    """

    all_stop_ids = Stop.objects.filter(
        Q(tasks__assignment__status=ASSIGNMENT_STATUS.PENDING)
        | Q(tasks__assignment__status=ASSIGNMENT_STATUS.CONFIRMED),
        driver=driver,
    ).values_list("stop_id", flat=True)

    all_stop_details = dict()
    sortable_stop_ids = list()

    for stop_id in set(all_stop_ids):
        details = {**Stop.objects.filter(stop_id=stop_id).values()[0]}
        details["tasks"] = Task.objects.filter(stop__stop_id=stop_id).values()
        details["assignments"] = Assignment.objects.filter(
            tasks__stop__stop_id=stop_id
        ).values()

        all_stop_details[stop_id] = details

        if stop_id not in excluded_stops:
            sortable_stop_ids.append(stop_id)

    return (sortable_stop_ids, all_stop_details)  # type: ignore


def _get_next_stop_id(
    previous_stop_id: Union[Driver, str],
    neighboring_stop_ids: List[str],
    all_stop_details: Dict[str, Any],
) -> Tuple[str, List[str]]:
    assert (
        len(neighboring_stop_ids) > 0
    ), f"Expected one or multiple neighboring stops but found {len(neighboring_stop_ids)}."

    if len(neighboring_stop_ids) == 1:
        return (neighboring_stop_ids[0], [])

    neighboring_stop_etas = []

    for stop_id in neighboring_stop_ids:
        if isinstance(previous_stop_id, Driver):
            estimated_duration = _get_estimated_duration_from_driver(
                previous_stop_id,
                all_stop_details[stop_id],
                timezone.now(),
            )
        else:
            estimated_duration = _get_estimated_duration_between_stops(
                all_stop_details[previous_stop_id],
                all_stop_details[stop_id],
                timezone.now(),
            )

        neighboring_stop_etas.append((stop_id, estimated_duration))

    neighboring_stop_etas.sort(key=lambda item: item[1])
    sorted_neighboring_stop_ids = [item[0] for item in neighboring_stop_etas]
    return (sorted_neighboring_stop_ids[0], sorted_neighboring_stop_ids[1:])


def _get_previous_stop_id(
    driver: Driver, sortable_stop_ids: List[str], excluded_stops: Dict[str, Any]
) -> Union[Driver, str]:
    total_stops = (
        Stop.objects.filter(
            Q(tasks__assignment__status=ASSIGNMENT_STATUS.PENDING)
            | Q(tasks__assignment__status=ASSIGNMENT_STATUS.CONFIRMED),
            driver=driver,
        )
        .distinct()
        .count()
    )
    if total_stops == len(set(sortable_stop_ids)):
        return driver

    # We are sorting a subset of the driver's stops. The previous stop is already part of the sorted stops.
    previous_stop = (
        Stop.objects.filter(stop_id__in=set([s["stop_id"] for s in excluded_stops.values()]))  # type: ignore
        .order_by("-ranking")
        .first()
    )
    return previous_stop.stop_id  # type: ignore


def _get_neighboring_stop_ids(
    sortable_stop_ids: List[str], all_stop_details: Dict[str, Any]
) -> List[str]:
    # Find all pickup and dropoff stops. We are using sets here because stops could occur multiple times
    # if for example two packages are coming from the same pickup stop.
    pickup_stop_ids = set()
    dropoff_stop_ids = set()

    for stop_id in sortable_stop_ids:
        details = all_stop_details[stop_id]
        for task in details["tasks"]:
            if task["type"] == TASK_TYPE.PICK_UP:
                pickup_stop_ids.add(stop_id)
            else:
                dropoff_stop_ids.add(stop_id)

    neighboring_stop_ids = list(pickup_stop_ids.difference(dropoff_stop_ids))
    if len(neighboring_stop_ids) > 0:
        return neighboring_stop_ids

    return list(set(sortable_stop_ids))


def _sort_stops_using_breadth_search(
    driver: Driver,
    sortable_stop_ids: List[str],
    all_stop_details: Dict[str, Any],
    excluded_stops: Dict[str, Any],
) -> List[str]:
    """
    If we have packages p1 (A -> B), p2 (A -> C), p3 (A -> D), p4 (B -> E) and p5 (M -> N),
    it's equivalent to having two n-trees rooted at A and M:

             A          M
           / | \        |
          B  C  D       N
          |
          E

    Assuming none of the packages were picked up yet, _get_or_create_stops function will
    create 7 stops that can be visualized like above. The goal of a sorting algorithm is to
    determine the order in which the driver will visit those stops. Obviusly we can't visit
    stops B/C/D/E/N without going first to A and M to pickup the package. So the driver has
    to start from either A or M, depending on which stop is closer to them; we are only using
    time-to-pickup to rank stops, but in the future we may introduce priority of the package in
    the mix.

    Depending on whether the driver goes to A first or M, they have to finish stops in the tree
    they picked before going to the next tree. In this case, if they start with A, we can then
    determine the which stop among B/C/D to visit then based on which one is closer to A. If we
    pick B, we can next decide on wether we go to E/C/D again based on which stop is closer to B.
    The circle goes on until we go through all the stops.

    Assuming the natural order these letters as the actual distance, the resulting stops path is
    A -> B -> C -> D -> E and M -> N.

    Below the concept of previous_stop_id and neighboring_stop_ids is pretty tricky. Initially,
    when the driver has not picked up any package, previous_stop_id is the actual driver's
    current location. And neighboring_stop_ids are the pickup stops they have to go to. However,
    once one or multiple packages have been picked up, the previous_stop_id could be the previous
    stop that the driver finished visiting (e.g. pickup stop). And the neighboring_stop_ids will be
    the stops they should go next to.

    Note1: in case we are dealing with picked up packages, this function will sort a subset of all
    the stops the driver is working with. For example, if the driver has completed stop A (i.e.
    picked up package p1, p2 and p3), this function will be sorting E, M and N stops as we don't
    want to mess up with the stop ranking of B/C/D to avoid delaying their delivery. That's so because
    we prioritize the driver finishing the delivery they started before picking another one. However,
    if only p1 and p2 were picked up (which implies the driver is still at the stop A waiting to pickup p3),
    this algorithm will sort all the stops.

    Note2: There may be a bug here if p1, p2 and p3 packages are picked up. We will only sort M and N, but E and D
    will be left unsorted.
    """
    previous_stop_id = _get_previous_stop_id(driver, sortable_stop_ids, excluded_stops)
    neighboring_stop_ids = _get_neighboring_stop_ids(
        sortable_stop_ids, all_stop_details
    )

    # Contruct a reverse mapping of assignment to stops
    assignment_to_stops = defaultdict(list)
    for stop_id, details in all_stop_details.items():
        for assignment in details["assignments"]:
            assignment_to_stops[assignment["assignment_id"]].append(stop_id)

    sorted_stop_ids = []
    visited_stop_ids = set([])

    while len(neighboring_stop_ids) > 0:
        next_stop_id, remaining_stop_ids = _get_next_stop_id(
            previous_stop_id, neighboring_stop_ids, all_stop_details
        )
        sorted_stop_ids.append(next_stop_id)
        visited_stop_ids.add(next_stop_id)

        # Discover neighboring stops connected to the next stop
        for assignment in all_stop_details[next_stop_id]["assignments"]:
            for stop_id in assignment_to_stops[assignment["assignment_id"]]:
                if stop_id not in visited_stop_ids and stop_id not in excluded_stops:
                    remaining_stop_ids.append(stop_id)

        neighboring_stop_ids = remaining_stop_ids
        previous_stop_id = next_stop_id

    return sorted_stop_ids


def _get_excluded_stops_from_sorting(driver: Driver) -> Dict[str, Any]:
    # We must exclude stops for confirmed assignments whose pickup tasks were completed. That is
    # required so that such assignments' stops can keep their existing ranking untouched so that
    # the driver can finish working on those assignments first before working on newly created
    # assignments. See https://azure-snowflake-e49.notion.site/Routing-v1-0-eb7345a55b944041895b5803949f63b7
    concluded_stops = Stop.objects.filter(
        driver=driver,
        completed_at__isnull=False,
        tasks__assignment__status=ASSIGNMENT_STATUS.CONFIRMED,
    )
    return {
        item["stop_id"]: item  # type: ignore
        for item in Stop.objects.filter(
            driver=driver,
            tasks__assignment__in=Assignment.objects.filter(
                tasks__stop__in=concluded_stops.all()
            ),
        )
        .values()
        .distinct()
    }


def _sort_pending_and_confirmed_stops(driver: Driver) -> None:
    excluded_stops = _get_excluded_stops_from_sorting(driver)
    sortable_stop_ids, all_stop_details = _get_sortable_stops(driver, excluded_stops)
    sorted_stop_ids = _sort_stops_using_breadth_search(
        driver, sortable_stop_ids, all_stop_details, excluded_stops
    )

    assert len(sorted_stop_ids) == len(
        set(sortable_stop_ids)
    ), f"Expected sorted_stop_ids to equal sortable_stop_ids but found {len(sorted_stop_ids)} != {len(sortable_stop_ids)}"

    ranking = 0
    if len(excluded_stops) > 0:
        ranking = max(map(lambda x: x["ranking"], excluded_stops.values())) + 1

    for stop_id in sorted_stop_ids:
        Stop.objects.filter(stop_id=stop_id).update(ranking=ranking)
        ranking += 1


def _send_new_assignment_notification(driver: Driver) -> None:
    push = {
        "receiverIds": [str(driver.driver_id)],
        "heading": "New Delivery!",
        "message": "Click to start a new assignment.",
        "jsonData": {
            "notificationType": "NEW_ASSIGNMENT",
        },
        "metadata": {
            "appId": settings.WEB_PUSH_DRIVER_APP_ID,
            "apiKey": settings.WEB_PUSH_DRIVER_API_KEY,
            "androidChannelId": settings.WEB_PUSH_NEW_ASSIGNMENT_CHANNEL_ID,
        },
    }
    communication_api.send_push(push)


@mini_huey.task()
def _update_arrive_and_depart_by_fields(location: Location) -> None:
    # Update current stops' arrive_by and depart_by. We are not relying on the stop's
    # ranking field to obtain the previous stop as the previous stop based on the
    # ranking field may corresponding to a newly created pending assignment that's not
    # included in this list we are confirming (i.e. case of a race condition). We only
    # want to estimate arrive & depart by fields using only stops for confirmed assignments.
    stops = list(get_current_stops(location.driver).all())

    for i in range(len(stops)):
        next_stop = stops[i]
        previous_stop = stops[i - 1] if i > 0 else None

        if previous_stop:
            origin = google.Coordinates(previous_stop.latitude, previous_stop.longitude)
        else:
            origin = google.Coordinates(location.latitude, location.longitude)

        destination = google.Coordinates(next_stop.latitude, next_stop.longitude)

        # Compute the estimated depart_by and arrive_by using the previous stop's values.
        # We are assuming that the driver should stop at most 5 minutes on each stop.
        depart_time = previous_stop.arrive_by if previous_stop else timezone.now()
        allowed_delay = 5 if previous_stop else 0
        depart_by = depart_time + timedelta(minutes=allowed_delay)
        arrive_by = google.get_estimated_arrival(origin, destination, depart_by)

        next_stop.depart_by = depart_by
        next_stop.arrive_by = arrive_by
        next_stop.save()


def _multisort_drivers(
    x: Tuple[Driver, int, datetime], y: Tuple[Driver, int, datetime]
) -> int:
    # Sort by estimated duration to pickup first
    if x[1] < y[1]:
        return -1

    if x[1] > y[1]:
        return 1

    # Then by last assignment completion
    if x[2] < y[2]:
        return -1

    if x[2] > y[2]:
        return 1

    # Both drivers have the same estimated duration and last assignment completion
    return 0


def send_cancelled_assignment_notification(driver: Driver) -> None:
    push = {
        "receiverIds": [str(driver.driver_id)],
        "heading": "CANCELED DELIVERY.",
        "message": "This delivery has been canceled.",
        "jsonData": {
            "notificationType": "CANCELLED_ASSIGNMENT",
        },
        "metadata": {
            "appId": settings.WEB_PUSH_DRIVER_APP_ID,
            "apiKey": settings.WEB_PUSH_DRIVER_API_KEY,
            "androidChannelId": settings.WEB_PUSH_CANCELLED_ASSIGNMENT_CHANNEL_ID,
        },
    }
    communication_api.send_push(push)


def confirm_assignments(location: Location, assignment_ids: List[str]) -> None:
    with transaction.atomic():
        for assignment_id in assignment_ids:
            # Assignment must be pending
            assignment = Assignment.objects.get(assignment_id=assignment_id)
            if assignment.status != ASSIGNMENT_STATUS.PENDING:
                raise exceptions.ValidationError(_("Assignment is no longer valid."))

            # Update assignment. Multiple assignents can share the same location
            # if for example a driver confirmed multiple assignents simultaneously
            assignment.confirmed_at = timezone.now()
            assignment.status = ASSIGNMENT_STATUS.CONFIRMED
            assignment.confirmation_location = location
            assignment.save()

            # Update location. This avoids overriding it with the "latest" driver position
            location.is_assigned = True
            location.save()

            # Package events are created asynchronously
            order_api.create_package_event(
                assignment.package_id,
                {
                    "eventName": "DRIVER_CONFIRMED",
                    "assignmentId": str(assignment.assignment_id),
                },
            )

    # Update arrive & depart by fields asynchronously as it makes external calls to google API
    _update_arrive_and_depart_by_fields(location)


def invalidate_assignments(status: str, assignments: List[Assignment]) -> None:
    with transaction.atomic():
        for assignment in assignments:
            # Update assignment
            assignment.status = status
            assignment.save()

            # Delete stops (if they don't have other tasks) as well as the actual tasks
            for task in assignment.tasks.all():
                if task.stop.tasks.count() == 1:
                    task.stop.delete()

                task.delete()

            # Update package synchronously so that we can fail this transaction if we can't update order-api
            order_api.update_package(
                assignment.package_id,
                {"driverId": None, "assignmentId": None},
            )


def get_current_stops(driver: Union[Driver, None] = None) -> QuerySet:
    """
    Current stops correspond to stops that the driver has to visit while working on the
    the assignents they confirmed. This function returns them in a sorted order; meaning
    the first stop in the results returned by this function, is the stop the driver should
    be (or is) heading to.

    A driver can have stops for pending and confirmed assignments simultaneously. While
    the ranking of stops is based on pending and confirmed assginments, this function only
    returns stops for confirmed assignments. As an example, if a driver has S1, S2, S3, S4
    stops and S2 and S3 are for pending assingment, this function will return S1 and S4, in
    that order.
    """

    qs = Stop.objects.filter(
        tasks__assignment__status=ASSIGNMENT_STATUS.CONFIRMED
    ).distinct()

    if not driver:
        return qs

    return qs.filter(driver=driver)


def depart_to_stop(stop: Stop) -> None:
    # Update stop
    stop.departed_at = timezone.now()
    stop.save()

    # Create relevant package event
    for task in stop.tasks.all():
        if task.type == TASK_TYPE.PICK_UP:
            order_api.create_package_event(
                task.assignment.package_id,
                {
                    "eventName": "DRIVER_DEPARTING_PICK_UP",
                    "assignmentId": str(task.assignment.assignment_id),
                },
            )
        else:
            order_api.create_package_event(
                task.assignment.package_id,
                {
                    "eventName": "DRIVER_DEPARTING_DROP_OFF",
                    "assignmentId": str(task.assignment.assignment_id),
                },
            )


def arrive_to_stop(stop: Stop) -> None:
    with transaction.atomic():
        # Update stop
        stop.arrived_at = timezone.now()
        stop.save()

        # Create a delay if necessary
        actual_duration = (stop.arrived_at - stop.departed_at).total_seconds()  # type: ignore
        expected_duration = (stop.arrive_by - stop.depart_by).total_seconds()  # type: ignore

        if (actual_duration * 1.05) > expected_duration:
            # We add a delay  if actual duration is 5% higher than the expected duration
            Delay.objects.create(
                driver=stop.driver,
                stop=stop,
                type=DELAY_TYPE.STOP,
                status=DELAY_STATUS.PENDING,
            )

    # Create relevant package event
    for task in stop.tasks.all():
        if task.type == TASK_TYPE.PICK_UP:
            order_api.create_package_event(
                task.assignment.package_id,
                {
                    "eventName": "DRIVER_ARRIVED_PICK_UP",
                    "assignmentId": str(task.assignment.assignment_id),
                },
            )
        else:
            order_api.create_package_event(
                task.assignment.package_id,
                {
                    "eventName": "DRIVER_ARRIVED_DROP_OFF",
                    "assignmentId": str(task.assignment.assignment_id),
                },
            )


def complete_task(task: Task) -> None:
    with transaction.atomic():
        # Update task
        task.completed_at = timezone.now()
        task.save()

        # Update assingment
        if all([t.completed_at != None for t in task.assignment.tasks.all()]):
            task.assignment.status = ASSIGNMENT_STATUS.COMPLETED
            task.assignment.save()

        # Update stop
        if all([t.completed_at != None for t in task.stop.tasks.all()]):
            task.stop.completed_at = timezone.now()
            task.stop.save()

        # TODO: Uncomment this block once we moved charges to package in order-api. Add unit tests too
        # # Add a charge to the customer
        # pickup_duration = (task.completed_at - task.stop.arrived_at).total_seconds()
        # if task.type == TASK_TYPE.PICK_UP and pickup_duration > 300:
        #     charge_amount = 200 * math.ceil(pickup_duration / 300)
        #     order_api.create_package_charge(
        #         task.assignment.package_id, {"totalAmount": charge_amount}
        #     )

    # Create relevant package event
    if task.type == TASK_TYPE.PICK_UP:
        order_api.create_package_event(
            task.assignment.package_id,
            {
                "eventName": "PACKAGE_PICKED_UP",
                "assignmentId": str(task.assignment.assignment_id),
            },
        )
    else:
        order_api.create_package_event(
            task.assignment.package_id,
            {
                "eventName": "PACKAGE_DELIVERED",
                "assignmentId": str(task.assignment.assignment_id),
            },
        )


def create_assignments(
    driver: Driver, type: str, package_ids: List[str]
) -> List[Assignment]:
    assignments = []

    with transaction.atomic():
        for package_id in package_ids:
            # Create assignment
            assignment = Assignment.objects.create(
                driver=driver,
                package_id=package_id,
                type=type,
                status=ASSIGNMENT_STATUS.PENDING,
            )

            # Create stops
            package = order_api.get_package(package_id)
            pickup_stop, dropoff_stop = _get_or_create_stops(
                driver,
                package["from_address"],
                package["to_address"],
            )

            # Create tasks
            Task.objects.create(
                type=TASK_TYPE.PICK_UP, stop=pickup_stop, assignment=assignment
            )
            Task.objects.create(
                type=TASK_TYPE.DROP_OFF, stop=dropoff_stop, assignment=assignment
            )

            assignments.append(assignment)

        # Sort the resulting stops
        _sort_pending_and_confirmed_stops(driver)

        for assignment in assignments:
            # Update package synchronously so that we can fail this transaction if we can't update order-api
            order_api.update_package(
                assignment.package_id,
                {
                    "driverId": str(driver.driver_id),
                    "assignmentId": str(assignment.assignment_id),
                },
            )

            # Create package event asynchronously
            order_api.create_package_event(
                assignment.package_id,
                {
                    "eventName": "DRIVER_ASSIGNED",
                    "assignmentId": str(assignment.assignment_id),
                },
            )

    # Notify driver asynchronously
    _send_new_assignment_notification(driver)

    return assignments


def find_assignable_driver(package: Dict[str, Any]) -> Union[Driver, None]:
    assigned_driver_ids = Assignment.objects.filter(
        Q(status=ASSIGNMENT_STATUS.PENDING) | Q(status=ASSIGNMENT_STATUS.CONFIRMED)
    ).values_list("driver_id", flat=True)

    eligible_drivers = Driver.objects.filter(
        status=DRIVER_STATUS.ACTIVE, is_available=True
    ).exclude(driver_id__in=assigned_driver_ids)

    if not eligible_drivers.exists():
        return None

    results = []
    now = timezone.now()

    for driver in eligible_drivers.all():
        duration_to_pickup = _get_estimated_duration_from_driver(
            driver,
            package["from_address"],
            now,
        )
        normalized_duration = int(duration_to_pickup / 60)  # Convert secons to minutes

        last_assignment = Assignment.objects.filter(
            driver=driver,
            status=ASSIGNMENT_STATUS.COMPLETED,
            created_at__date=now.date(),
        ).first()
        last_assignment_completion = (
            last_assignment.updated_at if last_assignment else now
        )

        results.append((driver, normalized_duration, last_assignment_completion))

    results.sort(key=cmp_to_key(_multisort_drivers))
    # Picks the driver closer to the pickup with older last assignment completion
    return results[0][0]
