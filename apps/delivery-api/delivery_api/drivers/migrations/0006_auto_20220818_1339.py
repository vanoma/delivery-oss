# Generated by Django 3.2.10 on 2022-08-18 13:39

from django.db import migrations, models
import vanoma_api_utils.django.fields


class Migration(migrations.Migration):

    dependencies = [
        ("drivers", "0005_driver_is_full_time"),
    ]

    operations = [
        migrations.AddField(
            model_name="location",
            name="battery_level",
            field=models.FloatField(default=0.0),
        ),
        migrations.AddField(
            model_name="location",
            name="is_gps_enabled",
            field=models.BooleanField(default=False),
        ),
        migrations.AddField(
            model_name="location",
            name="location_access_status",
            field=vanoma_api_utils.django.fields.StringField(
                choices=[
                    ("DENIED", "DENIED"),
                    ("ALLOWED_ALWAYS", "ALLOWED_ALWAYS"),
                    ("ALLOWED_WHEN_IN_USE", "ALLOWED_WHEN_IN_USE"),
                ],
                default="DENIED",
                max_length=40,
            ),
        ),
        migrations.AddField(
            model_name="location",
            name="is_location_service_enabled",
            field=models.BooleanField(default=False),
        ),
    ]
