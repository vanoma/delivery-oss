from typing import Any, Dict
from django.db import transaction
from vanoma_api_utils.rest_framework import serializers
from delivery_api.drivers.utils import auth_api, communication_api
from .models import Staff


class StaffSerializerV1_0(serializers.BaseModelSerializer):
    class Meta:
        model = Staff
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

    def create(self, validated_data: Dict[str, Any]) -> Staff:
        communication_api.verify_otp(
            self.initial_data["otp_id"],
            self.initial_data["otp_code"],
            self.initial_data["phone_number"],
        )

        with transaction.atomic():
            staff = super().create(validated_data)
            auth_api.create_login(
                {
                    "type": "STAFF",
                    "staff_id": str(staff.staff_id),
                    "phone_number": staff.phone_number,
                    "password": self.initial_data["password"],
                }
            )
            return staff
