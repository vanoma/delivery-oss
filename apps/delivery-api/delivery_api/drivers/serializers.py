from typing import Any, Dict
from django.db import transaction
from vanoma_api_utils.rest_framework import serializers
from delivery_api.deliveries.utils.constants import ASSIGNMENT_STATUS
from .models import Driver, Location
from .utils import auth_api, communication_api


class LocationSerializerV1_0(serializers.BaseModelSerializer):
    latitude = serializers.FloatField()
    longitude = serializers.FloatField()

    class Meta:
        model = Location
        exclude = ["driver"]
        read_only_fields = [
            "is_assigned",
            "created_at",
            "updated_at",
        ]

    def create(self, validated_data: Dict[str, Any]) -> Location:
        with transaction.atomic():
            location = Location.objects.filter(
                driver=validated_data["driver"], is_assigned=False
            ).first()

            if not location:
                # All locations have an assignment. We can't override them.
                return super().create(validated_data)

            # Override the latest location without assignment to avoid creating
            # lots of unnessary locations. This is merely an optimization.
            return self.update(location, validated_data)


class DriverSerializerV1_0(serializers.BaseModelSerializer):
    is_assignable = serializers.BooleanField(read_only=True)
    latest_location = LocationSerializerV1_0(read_only=True)
    assignment_count = serializers.SerializerMethodField()

    class Meta:
        model = Driver
        fields = "__all__"
        read_only_fields = [
            "created_at",
            "updated_at",
        ]

    def validate(self, attrs: Dict[str, Any]) -> Dict[str, Any]:
        if self.instance is None:
            if self.initial_data.get("password", None) is None:
                raise serializers.ValidationError("Missing required password attribute")

            if self.initial_data.get("otp_id", None) is None:
                raise serializers.ValidationError("Missing required otpId attribute")

            if self.initial_data.get("otp_code", None) is None:
                raise serializers.ValidationError("Missing required otpCode attribute")

        return attrs

    def create(self, validated_data: Dict[str, Any]) -> Driver:
        communication_api.verify_otp(
            self.initial_data["otp_id"],
            self.initial_data["otp_code"],
            self.initial_data["phone_number"],
        )

        with transaction.atomic():
            driver = super().create(validated_data)
            auth_api.create_login(
                {
                    "type": "DRIVER",
                    "driverId": str(driver.driver_id),
                    "phoneNumber": driver.phone_number,
                    "password": self.initial_data["password"],
                }
            )
            return driver

    def get_assignment_count(self, driver: Driver) -> int:
        return driver.assignments.filter(status=ASSIGNMENT_STATUS.CONFIRMED).count()
