class DELAY_TYPE:
    STOP = "STOP"
    ASSIGNMENT = "ASSIGNMENT"

    ALL = [
        (STOP, STOP),
        (ASSIGNMENT, ASSIGNMENT),
    ]


class DELAY_STATUS:
    PENDING = "PENDING"
    CONFIRMED = "CONFIRMED"
    JUSTIFIED = "JUSTIFIED"

    ALL = [
        (PENDING, PENDING),
        (CONFIRMED, CONFIRMED),
        (JUSTIFIED, JUSTIFIED),
    ]


class ASSIGNMENT_TYPE:
    MANUAL = "MANUAL"
    AUTOMATIC = "AUTOMATIC"

    ALL = [(MANUAL, MANUAL), (AUTOMATIC, AUTOMATIC)]


class ASSIGNMENT_STATUS:
    """
    An assignment starts in pending status then goes to confirmed once the driver confirms/accepts it.
    Pending assignments have incomplete tasks whereas confirmed assignment may or may not have at least
    one task (the pickup task) completed.

    Assignment is set to completed status when all the two tasks (pickup and dropoff) associated with it
    are completed. Expired assignments corresponds to assignment that were not confirmed by the driver within
    alloted time. Cancelled assignments corresponds to assignments that were cancelled by customer support or
    customers.
    """

    PENDING = "PENDING"
    CONFIRMED = "CONFIRMED"
    COMPLETED = "COMPLETED"
    EXPIRED = "EXPIRED"
    CANCELED = "CANCELED"

    ALL = [
        (PENDING, PENDING),
        (CONFIRMED, CONFIRMED),
        (COMPLETED, COMPLETED),
        (EXPIRED, EXPIRED),
        (CANCELED, CANCELED),
    ]


class TASK_TYPE:
    PICK_UP = "PICK_UP"
    DROP_OFF = "DROP_OFF"

    ALL = [
        (PICK_UP, PICK_UP),
        (DROP_OFF, DROP_OFF),
    ]


class CACHE_KEY:
    AUTO_ASSIGNMENT_FINISHED_AT = "AUTO_ASSIGNMENT_FINISHED_AT"
    IS_AUTO_ASSIGNMENT_RUNNING = "IS_AUTO_ASSIGNMENT_RUNNING"
