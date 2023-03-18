from typing import Any, Dict, List, Union
from vanoma_api_utils.rest_framework import serializers
from delivery_api.deliveries.utils.constants import ASSIGNMENT_STATUS
from delivery_api.drivers.models import Driver, Location
from delivery_api.drivers.serializers import (
    DriverSerializerV1_0,
    LocationSerializerV1_0,
)
from .models import Assignment, Delay, Stop, Task
from .utils.fields import MP3Base64FileField


class StopSerializerV1_0(serializers.BaseModelSerializer):
    class Meta:
        model = Stop
        exclude = ["driver"]


class TaskSerializerV1_0(serializers.BaseModelSerializer):
    stop = StopSerializerV1_0(queryset=Stop.objects.all())

    class Meta:
        model = Task
        exclude = ["assignment"]


class AssignmentSerializerV1_0(serializers.BaseModelSerializer):
    confirmation_location = LocationSerializerV1_0(queryset=Location.objects.all())
    driver = DriverSerializerV1_0(queryset=Driver.objects.all())
    tasks = serializers.SerializerMethodField()

    class Meta:
        model = Assignment
        fields = "__all__"

    def get_tasks(self, obj: Assignment) -> List[Dict[str, Any]]:
        serializer = TaskSerializerV1_0(obj.tasks.order_by("stop__ranking"), many=True)  # type: ignore
        return serializer.data  # type: ignore


class CurrentAssignmentSerializerV1_0(serializers.BaseModelSerializer):
    driver = DriverSerializerV1_0(queryset=Driver.objects.all())
    package = serializers.SerializerMethodField()

    class Meta:
        model = Assignment
        exclude = ["confirmation_location", "package_id"]

    def get_package(self, obj: Assignment) -> Dict[str, Any]:
        return self.context["packages"][obj.package_id]


class CurrentTaskSerializerV1_0(serializers.BaseModelSerializer):
    package = serializers.SerializerMethodField()

    class Meta:
        model = Task
        exclude = ["assignment", "stop"]

    def get_package(self, obj: Task) -> Dict[str, Any]:
        return self.context["packages"][obj.assignment.package_id]


class CurrentStopSerializerV1_0(serializers.BaseModelSerializer):
    current_tasks = serializers.SerializerMethodField()

    class Meta:
        model = Stop
        exclude = ["driver"]

    def get_current_tasks(self, obj: Stop) -> List[Dict[str, Any]]:
        serializer = CurrentTaskSerializerV1_0(obj.tasks.filter(assignment__status=ASSIGNMENT_STATUS.CONFIRMED).order_by("type"), many=True, context=self.context)  # type: ignore
        return serializer.data  # type: ignore


class DelaySerializerV1_0(serializers.BaseModelSerializer):
    driver = DriverSerializerV1_0(queryset=Driver.objects.all())
    stop = serializers.SerializerMethodField()
    assignment = AssignmentSerializerV1_0(queryset=Assignment.objects.all())
    justification = MP3Base64FileField()

    class Meta:
        model = Delay
        fields = "__all__"

    def get_stop(self, delay: Delay) -> Union[Dict[str, Any], None]:
        if not delay.stop:
            return None

        return {
            "stopId": str(delay.stop.stop_id),
            "ranking": delay.stop.ranking,
            "latitude": delay.stop.latitude,
            "longitude": delay.stop.longitude,
            "departBy": delay.stop.depart_by,
            "arriveBy": delay.stop.arrive_by,
            "departedAt": delay.stop.departed_at,
            "arrivedAt": delay.stop.arrived_at,
            "createdAt": delay.stop.created_at,
            "updatedAt": delay.stop.updated_at,
        }
