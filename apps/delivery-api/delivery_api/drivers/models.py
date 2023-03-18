from datetime import timedelta
from django.db import models
from django.utils import timezone
from vanoma_api_utils.django import fields
from .utils.constants import DRIVER_STATUS, LOCATION_ACCESS_STATUS


class Driver(models.Model):
    driver_id = fields.PrimaryKeyField()
    first_name = fields.StringField()
    last_name = fields.StringField()
    phone_number = fields.PhoneNumberField(unique=True)
    second_phone_number = fields.PhoneNumberField()
    status = fields.StringField(
        choices=DRIVER_STATUS.ALL, default=DRIVER_STATUS.PENDING
    )
    is_available = models.BooleanField(default=False)
    is_full_time = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    @property
    def full_name(self) -> str:
        return f"{self.first_name} {self.last_name}"

    @property
    def latest_location(self) -> "Location | None":
        try:
            return self.locations.latest("created_at")
        except Location.DoesNotExist:
            return None

    @property
    def is_assignable(self) -> bool:
        if not self.is_available:
            return False

        if not self.latest_location:
            return False

        if self.latest_location.updated_at + timedelta(minutes=3) < timezone.now():
            return False

        return True

    class Meta:
        db_table = "driver"


class Location(models.Model):
    location_id = fields.PrimaryKeyField()
    driver = models.ForeignKey(
        Driver, on_delete=models.CASCADE, related_name="locations"
    )
    is_assigned = models.BooleanField(default=False)
    # Could have used django's PointField but we'd need to install GDAL library
    # Using precision of 11 instead of the recommend 9 as per https://stackoverflow.com/a/30711177
    latitude = models.DecimalField(max_digits=11, decimal_places=8)
    longitude = models.DecimalField(max_digits=11, decimal_places=8)
    battery_level = models.FloatField(default=0.0)
    is_gps_enabled = models.BooleanField(default=False)
    is_location_service_enabled = models.BooleanField(default=False)
    location_access_status = fields.StringField(
        choices=LOCATION_ACCESS_STATUS.ALL, default=LOCATION_ACCESS_STATUS.DENIED
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "location"
