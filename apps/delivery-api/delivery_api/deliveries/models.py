from django.db import models
from vanoma_api_utils.django import fields
from delivery_api.drivers.models import Driver, Location
from .utils.constants import (
    DELAY_TYPE,
    DELAY_STATUS,
    ASSIGNMENT_TYPE,
    ASSIGNMENT_STATUS,
    TASK_TYPE,
)
from .utils.functions import resolve_upload_to


class Stop(models.Model):
    stop_id = fields.PrimaryKeyField()
    driver = models.ForeignKey(Driver, on_delete=models.CASCADE, related_name="stops")
    ranking = models.PositiveSmallIntegerField(null=True)
    latitude = models.FloatField()
    longitude = models.FloatField()
    completed_at = models.DateTimeField(null=True)
    depart_by = models.DateTimeField(null=True)
    arrive_by = models.DateTimeField(null=True)
    departed_at = models.DateTimeField(null=True)
    arrived_at = models.DateTimeField(null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "stop"


class Assignment(models.Model):
    assignment_id = fields.PrimaryKeyField()
    driver = models.ForeignKey(
        Driver, on_delete=models.CASCADE, related_name="assignments"
    )
    package_id = fields.StringField()
    confirmation_location = models.ForeignKey(
        Location, on_delete=models.CASCADE, null=True, related_name="assignments"
    )
    type = fields.StringField(choices=ASSIGNMENT_TYPE.ALL)
    status = fields.StringField(choices=ASSIGNMENT_STATUS.ALL)
    confirmed_at = models.DateTimeField(null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "assignment"


class Task(models.Model):
    task_id = fields.PrimaryKeyField()
    stop = models.ForeignKey(Stop, on_delete=models.CASCADE, related_name="tasks")
    assignment = models.ForeignKey(
        Assignment, on_delete=models.CASCADE, related_name="tasks"
    )
    type = fields.StringField(choices=TASK_TYPE.ALL)
    completed_at = models.DateTimeField(null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "task"
        constraints = [
            models.UniqueConstraint(
                fields=["stop", "assignment", "type"],
                name="unique_task_type_for_assignment_and_stop",
            )
        ]


class Delay(models.Model):
    delay_id = fields.PrimaryKeyField()
    driver = models.ForeignKey(Driver, on_delete=models.CASCADE, related_name="delays")
    stop = models.ForeignKey(
        Stop, on_delete=models.CASCADE, null=True, related_name="delays"
    )
    assignment = models.ForeignKey(
        Assignment, on_delete=models.CASCADE, null=True, related_name="delays"
    )
    type = fields.StringField(choices=DELAY_TYPE.ALL)
    status = fields.StringField(choices=DELAY_STATUS.ALL)
    justification = models.FileField(upload_to=resolve_upload_to)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "delay"
