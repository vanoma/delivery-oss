# Generated by Django 3.2.10 on 2022-05-30 13:17

from django.db import migrations
import vanoma_api_utils.django.fields
import vanoma_api_utils.django.validators


class Migration(migrations.Migration):

    dependencies = [
        ("drivers", "0002_driver_status"),
    ]

    operations = [
        migrations.AddField(
            model_name="driver",
            name="second_phone_number",
            field=vanoma_api_utils.django.fields.PhoneNumberField(
                default="250783631488",
                max_length=40,
                validators=[vanoma_api_utils.django.validators.validate_number],
            ),
            preserve_default=False,
        ),
    ]
