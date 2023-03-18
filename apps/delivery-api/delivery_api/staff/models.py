from django.db import models
from vanoma_api_utils.django import fields
from .utils.constants import STAFF_STATUS


class Staff(models.Model):
    staff_id = fields.PrimaryKeyField()
    first_name = fields.StringField()
    last_name = fields.StringField()
    phone_number = fields.PhoneNumberField(unique=True)
    status = fields.StringField(choices=STAFF_STATUS.ALL, default=STAFF_STATUS.PENDING)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "staff"
