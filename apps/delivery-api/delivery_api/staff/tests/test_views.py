import re
import json
import requests_mock
from uuid import uuid4
from django.urls import reverse
from django.test import override_settings
from rest_framework import status
from vanoma_api_utils.tests import faker
from vanoma_api_utils.misc import create_api_error
from vanoma_api_utils.constants import ERROR_CODE
from djangorestframework_camel_case.util import camelize  # type: ignore
from delivery_api.staff.models import Staff
from delivery_api.staff.utils.constants import STAFF_STATUS
from . import APITestCaseV1_0


@override_settings(
    VANOMA_AUTH_API_URL="http://auth-api",
    VANOMA_COMMUNICATION_API_URL="http://communication-api",
)
class StaffViewSetTestCaseV1_0(APITestCaseV1_0):
    def test_get_all_staff(self) -> None:
        staff = [self.create_staff(status=item[0]) for item in STAFF_STATUS.ALL]

        url = reverse("staff-list")
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(3, [self.render_staff(s) for s in staff])
        self.assertDictEqual(actual, expected)

    def test_get_one_staff(self) -> None:
        staff = self.create_staff()

        url = reverse("staff-detail", args=(staff.staff_id,))
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_staff(staff)
        self.assertDictEqual(actual, expected)

    def test_create_staff(self) -> None:
        otp_id = str(uuid4())
        otp_code = "12345"
        phone_number = "250788348456"

        with requests_mock.Mocker() as mocker:
            mocker.post(
                f"http://communication-api/otp/{otp_id}/verification",
                status_code=status.HTTP_204_NO_CONTENT,
            )
            mocker.post(
                "http://auth-api/login-creation",
                status_code=status.HTTP_200_OK,
            )

            data = {
                "firstName": faker.first_name(),
                "lastName": faker.first_name(),
                "phoneNumber": phone_number,
                "password": "password",
                "otpId": otp_id,
                "otpCode": otp_code,
            }

            url = reverse("staff-list")
            response = self.client.post(url, data=data)
            self.assertEqual(response.status_code, status.HTTP_201_CREATED)

            # Validate created staff
            staff = Staff.objects.get(phone_number=phone_number)
            actual = json.loads(response.content)
            expected = self.render_staff(staff)
            self.assertDictEqual(actual, expected)

            # Validate request to communication-api
            actual_otp_payload = json.loads(mocker.request_history[0].body)
            expected_otp_payload = {"otpCode": otp_code, "phoneNumber": phone_number}
            self.assertDictEqual(actual_otp_payload, expected_otp_payload)

            # Validate request to auth-api
            actual_login_payload = json.loads(mocker.request_history[1].body)
            expected_login_payload = {
                "type": "STAFF",
                "staffId": staff.staff_id,
                "phoneNumber": "250788348456",
                "password": "password",
            }
            self.assertDictEqual(actual_login_payload, expected_login_payload)

    def test_create_staff__returns_error_if_auth_api_request_failed(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.post(
                re.compile("http://communication-api/otp/*"),
                status_code=status.HTTP_204_NO_CONTENT,
            )
            mocker.post(
                "http://auth-api/login-creation",
                status_code=status.HTTP_400_BAD_REQUEST,
                json=camelize(
                    create_api_error(
                        ERROR_CODE.INVALID_REQUEST, "Bad create staff request"
                    )
                ),
            )

            data = {
                "firstName": faker.first_name(),
                "lastName": faker.first_name(),
                "phoneNumber": "250788348456",
                "password": "password",
                "otpId": str(uuid4()),
                "otpCode": "12345",
            }

            url = reverse("staff-list")
            response = self.client.post(url, data=data)
            self.assertEqual(
                response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR
            )

            # Validate response
            actual = json.loads(response.content)
            expected = {
                "errorCode": "INTERNAL_ERROR",
                "errorMessage": "Unable to update package on order-api: {'errorCode': 'INVALID_REQUEST', 'errorMessage': 'Bad create staff request'}",
            }
            self.assertDictEqual(actual, expected)

            # No staff should be created
            self.assertEqual(Staff.objects.count(), 0)

    def test_create_staff__returns_error_if_otp_verification_failed(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.post(
                re.compile("http://communication-api/otp/*"),
                status_code=status.HTTP_400_BAD_REQUEST,
                json=camelize(
                    create_api_error(
                        ERROR_CODE.INVALID_REQUEST, "Bad otp verification request"
                    )
                ),
            )

            data = {
                "firstName": faker.first_name(),
                "lastName": faker.first_name(),
                "phoneNumber": "250788348456",
                "password": "password",
                "otpId": str(uuid4()),
                "otpCode": "12345",
            }

            url = reverse("staff-list")
            response = self.client.post(url, data=data)
            self.assertEqual(
                response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR
            )

            # Validate response
            actual = json.loads(response.content)
            expected = {
                "errorCode": "INTERNAL_ERROR",
                "errorMessage": "OTP verification failed: {'errorCode': 'INVALID_REQUEST', 'errorMessage': 'Bad otp verification request'}",
            }
            self.assertDictEqual(actual, expected)

            # No staff should be created
            self.assertEqual(Staff.objects.count(), 0)

    def test_update_staff(self) -> None:
        staff = self.create_staff()
        data = {
            "firstName": "New First Name",
            "lastName": "New Last Name",
        }

        url = reverse("staff-detail", args=(staff.staff_id,))
        response = self.client.patch(url, data=data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        staff.refresh_from_db()
        actual = json.loads(response.content)
        expected = self.render_staff(staff)
        self.assertDictEqual(actual, expected)

        self.assertEqual(staff.first_name, "New First Name")
        self.assertEqual(staff.last_name, "New Last Name")
