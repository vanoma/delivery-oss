import logging
from datetime import timedelta
from django.utils import timezone
from django.core.cache import cache
from huey import crontab  # type: ignore
from dateutil.parser import isoparse
from delivery_api.deliveries.models import Assignment
from delivery_api.deliveries.utils.constants import ASSIGNMENT_STATUS, ASSIGNMENT_TYPE
from delivery_api.huey import mini_huey
from .utils import order_api
from .utils.constants import CACHE_KEY
from .workflows import (
    invalidate_assignments,
    create_assignments,
    find_assignable_driver,
)

FORMAT = "%Y-%m-%dT%H:%M:%Sz"


def assign_drivers_to_packages() -> None:
    # Cancel assignments that were not picked up previously. Notice that we are using
    # seconds as the precision of deciding whether we should mark the assignment as
    # expired. This avoids the likelihood of missing to expire an assigment because of
    # milliseconds difference with timezone.now(). The check on assignment creation is
    # needed to avoid expiring assignments created by customer support prematurely.
    for assignment in Assignment.objects.filter(status=ASSIGNMENT_STATUS.PENDING):
        created_at = isoparse(assignment.created_at.strftime(FORMAT))
        expires_at = created_at + timedelta(minutes=3)
        if expires_at <= timezone.now():
            invalidate_assignments(ASSIGNMENT_STATUS.EXPIRED, [assignment])

    # Create new assignments. Only customer support can do batching; so we will be doing
    # one assignment per driver here.
    placed_packages = order_api.get_packages(
        {
            "is_assigned": "false",
            "is_assignable": "true",
            "status": "PLACED",
            "sort": "pickUpStart",
        }
    )
    for package in placed_packages:
        package_id = package["package_id"]
        is_express = package["is_express"]

        has_pending_assignment = Assignment.objects.filter(
            package_id=package_id, status=ASSIGNMENT_STATUS.PENDING
        ).exists()

        # Express delivery assignment 20 minutes BEFORE pickup time
        # Two-hour delivery assignment 35 minutes AFTER pickup time
        assignment_time = 20 if is_express else -35

        is_ready_for_pickup = (
            isoparse(package["pick_up_start"]) - timedelta(minutes=assignment_time)
        ) <= timezone.now()

        if has_pending_assignment or not is_ready_for_pickup:
            logging.warning(
                f"Skipping automatic assignment for tn={package['tracking_number']}. has_pending_assignment={has_pending_assignment} is_ready_for_pickup={is_ready_for_pickup}"
            )
            continue  # Go to the next package

        driver = find_assignable_driver(package)
        if not driver:
            logging.warning(
                f"No driver found to assign package with tn={package['tracking_number']}. Are we out of drivers?"
            )
            break  # No need to process further packages

        logging.info(
            f"Assigning package tn={package['tracking_number']} to {driver.full_name}"
        )
        create_assignments(driver, ASSIGNMENT_TYPE.AUTOMATIC, [package_id])


@mini_huey.task(crontab(minute="*", strict=True))
def assign_drivers_to_packages_scheduler() -> None:
    """
    A work-around to fix timing issues in automatic assigment task. Automatic assignment
    task should run every 3 minutes and while running, it should also cancel assignments
    that were not picked up in 3 minutes. This means that if the job runs at 11:30 and
    takes 1 minute to finish, the next job run at 11:33 will miss pending assignments that
    we are created between 11:30:00 - 11:34:00 (i.e. basically when the job last run).

    A great fix would be to count the 3 minutes interval (i.e. when the job will run next)
    based on the last time the job completed. Using the example above, the next run will be
    11:34 instead of 11:30. The backgrounding task lib we use however does not support such
    advanced usage. The work-around here is to have a scheduler task which runs every minute
    then execute the actual automatic assignment task every 3 minutes based on when it was
    last completed.
    """

    default = (timezone.now() - timedelta(minutes=4)).strftime(FORMAT)
    auto_assignment_finished_at = cache.get(
        CACHE_KEY.AUTO_ASSIGNMENT_FINISHED_AT, default
    )

    is_auto_assignment_running = cache.get(CACHE_KEY.IS_AUTO_ASSIGNMENT_RUNNING, False)
    next_scheduled_run = isoparse(auto_assignment_finished_at) + timedelta(minutes=3)

    if not is_auto_assignment_running and next_scheduled_run <= timezone.now():
        cache.set(CACHE_KEY.IS_AUTO_ASSIGNMENT_RUNNING, True, timeout=None)

        try:
            logging.info("Running automatic assignment task")
            assign_drivers_to_packages()
        finally:
            cache.set_many(
                {
                    CACHE_KEY.IS_AUTO_ASSIGNMENT_RUNNING: False,
                    CACHE_KEY.AUTO_ASSIGNMENT_FINISHED_AT: timezone.now().strftime(
                        FORMAT
                    ),
                },
                timeout=None,
            )
