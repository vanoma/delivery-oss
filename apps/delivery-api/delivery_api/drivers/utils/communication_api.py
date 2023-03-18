from typing import Any, Dict
from requests import Response
from django.conf import settings
from django.utils.translation import gettext_lazy as _
from vanoma_api_utils.http import client
from delivery_api.huey import mini_huey
from djangorestframework_camel_case.util import camelize  # type: ignore


@mini_huey.task()
def send_sms(data: Dict[str, Any]) -> Response:
    return client.post(
        f"{settings.VANOMA_COMMUNICATION_API_URL}/sms", data=camelize(data)
    )


@mini_huey.task()
def send_push(data: Dict[str, Any]) -> Response:
    return client.post(
        f"{settings.VANOMA_COMMUNICATION_API_URL}/push", data=camelize(data)
    )


def verify_otp(otp_id: str, otp_code: str, phone_number: str) -> Response:
    response = client.post(
        f"{settings.VANOMA_COMMUNICATION_API_URL}/otp/{otp_id}/verification",
        data={"otpCode": otp_code, "phoneNumber": phone_number},
    )

    if not response.ok:
        raise Exception(_(f"OTP verification failed: {response.json()}"))

    return response
