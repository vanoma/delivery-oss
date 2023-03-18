# Generated by Django 3.2.10 on 2022-03-18 15:42

from django.db import migrations, models
import django.db.models.deletion
import uuid
import vanoma_api_utils.django.fields
import vanoma_api_utils.django.validators


class Migration(migrations.Migration):

    initial = True

    operations = [
        migrations.CreateModel(
            name="Driver",
            fields=[
                (
                    "driver_id",
                    vanoma_api_utils.django.fields.StringField(
                        max_length=40, primary_key=True, serialize=False, unique=True
                    ),
                ),
                (
                    "first_name",
                    vanoma_api_utils.django.fields.StringField(max_length=40),
                ),
                (
                    "last_name",
                    vanoma_api_utils.django.fields.StringField(max_length=40),
                ),
                (
                    "phone_number",
                    vanoma_api_utils.django.fields.PhoneNumberField(
                        max_length=40,
                        validators=[vanoma_api_utils.django.validators.validate_number],
                    ),
                ),
                ("is_available", models.BooleanField(default=False)),
                ("created_at", models.DateTimeField(auto_now_add=True)),
                ("updated_at", models.DateTimeField(auto_now=True)),
            ],
            options={
                "db_table": "driver",
            },
        ),
        migrations.CreateModel(
            name="Location",
            fields=[
                (
                    "location_id",
                    vanoma_api_utils.django.fields.PrimaryKeyField(
                        default=uuid.uuid4,
                        editable=False,
                        max_length=40,
                        primary_key=True,
                        serialize=False,
                    ),
                ),
                ("is_assigned", models.BooleanField(default=False)),
                ("latitude", models.DecimalField(decimal_places=8, max_digits=11)),
                ("longitude", models.DecimalField(decimal_places=8, max_digits=11)),
                ("created_at", models.DateTimeField(auto_now_add=True)),
                ("updated_at", models.DateTimeField(auto_now=True)),
                (
                    "driver",
                    models.ForeignKey(
                        on_delete=django.db.models.deletion.CASCADE,
                        related_name="locations",
                        to="drivers.driver",
                    ),
                ),
            ],
            options={
                "db_table": "location",
            },
        ),
    ]