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
from vanoma_api_utils.django.tests import load_fixture
from djangorestframework_camel_case.util import camelize  # type: ignore
from delivery_api.deliveries.utils.constants import TASK_TYPE
from delivery_api.drivers.models import Driver, Location
from delivery_api.drivers.utils.constants import DRIVER_STATUS
from . import APITestCaseV1_0


@override_settings(
    VANOMA_AUTH_API_URL="http://auth-api",
    VANOMA_ORDER_API_URL="http://order-api",
    VANOMA_COMMUNICATION_API_URL="http://communication-api",
)
class DriverViewSetTestCaseV1_0(APITestCaseV1_0):
    def test_get_all_drivers(self) -> None:
        drivers = [self.create_driver(status=item[0]) for item in DRIVER_STATUS.ALL]

        url = reverse("driver-list")
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(3, [self.render_driver(d) for d in drivers])
        self.assertDictEqual(actual, expected)

    def test_get_all_drivers__filters_by_status(self) -> None:
        drivers = [self.create_driver(status=item[0]) for item in DRIVER_STATUS.ALL]

        url = f"{reverse('driver-list')}?status=ACTIVE,PENDING"
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(
            2, [self.render_driver(drivers[0]), self.render_driver(drivers[1])]
        )
        self.assertDictEqual(actual, expected)

    def test_get_all_drivers__sorts_by_first_name(self) -> None:
        drivers = [
            self.create_driver(first_name="Zibra"),
            self.create_driver(first_name="Dog"),
        ]

        url = f"{reverse('driver-list')}?sort=first_name"
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(
            2, [self.render_driver(drivers[1]), self.render_driver(drivers[0])]
        )
        self.assertDictEqual(actual, expected)

    def test_get_one_driver(self) -> None:
        driver = self.create_driver()

        url = reverse("driver-detail", args=(driver.driver_id,))
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_driver(driver)
        self.assertDictEqual(actual, expected)

    def test_create_driver(self) -> None:
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
                "secondPhoneNumber": phone_number,
                "password": "password",
                "otpId": otp_id,
                "otpCode": otp_code,
            }

            url = reverse("driver-list")
            response = self.client.post(url, data=data)
            self.assertEqual(response.status_code, status.HTTP_201_CREATED)

            # Validate created driver
            driver = Driver.objects.get(phone_number=phone_number)
            actual = json.loads(response.content)
            expected = self.render_driver(driver)
            self.assertDictEqual(actual, expected)

            # Validate request to communication-api
            actual_otp_payload = json.loads(mocker.request_history[0].body)
            expected_otp_payload = {"otpCode": otp_code, "phoneNumber": phone_number}
            self.assertDictEqual(actual_otp_payload, expected_otp_payload)

            # Validate request to auth-api
            actual_login_payload = json.loads(mocker.request_history[1].body)
            expected_login_payload = {
                "type": "DRIVER",
                "driverId": driver.driver_id,
                "phoneNumber": "250788348456",
                "password": "password",
            }
            self.assertDictEqual(actual_login_payload, expected_login_payload)

    def test_create_driver__returns_error_if_auth_api_request_failed(self) -> None:
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
                        ERROR_CODE.INVALID_REQUEST, "Bad create driver request"
                    )
                ),
            )

            data = {
                "firstName": faker.first_name(),
                "lastName": faker.first_name(),
                "phoneNumber": "250788348456",
                "secondPhoneNumber": "250788348456",
                "password": "password",
                "otpId": str(uuid4()),
                "otpCode": "12345",
            }

            url = reverse("driver-list")
            response = self.client.post(url, data=data)
            self.assertEqual(
                response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR
            )

            # Validate response
            actual = json.loads(response.content)
            expected = {
                "errorCode": "INTERNAL_ERROR",
                "errorMessage": "Unable to update package on order-api: {'errorCode': 'INVALID_REQUEST', 'errorMessage': 'Bad create driver request'}",
            }
            self.assertDictEqual(actual, expected)

            # No driver should be created
            self.assertEqual(Driver.objects.count(), 0)

    def test_create_driver__returns_error_if_otp_verification_failed(self) -> None:
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
                "secondPhoneNumber": "250788348456",
                "password": "password",
                "otpId": str(uuid4()),
                "otpCode": "12345",
            }

            url = reverse("driver-list")
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

            # No driver should be created
            self.assertEqual(Driver.objects.count(), 0)

    def test_update_driver(self) -> None:
        driver = self.create_driver()
        data = {
            "firstName": "New First Name",
            "lastName": "New Last Name",
        }

        url = reverse("driver-detail", args=(driver.driver_id,))
        response = self.client.patch(url, data=data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        driver.refresh_from_db()
        actual = json.loads(response.content)
        expected = self.render_driver(driver)
        self.assertDictEqual(actual, expected)

        self.assertEqual(driver.first_name, "New First Name")
        self.assertEqual(driver.last_name, "New Last Name")

    def test_create_location__with_an_existing_assigned_location(self) -> None:
        location = self.create_location(is_assigned=True)
        data = {
            "latitude": -1.9353231,
            "longitude": 30.0692974,
            "batteryLevel": 0.8,
            "isGpsEnabled": True,
            "isLocationServiceEnabled": True,
            "locationAccessStatus": "ALLOWED_WHEN_IN_USE",
        }

        url = reverse(
            "driver-locations",
            args=(location.driver.driver_id,),
        )
        response = self.client.post(url, data=data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(Location.objects.count(), 2)

        location = Location.objects.latest("created_at")
        actual = json.loads(response.content)
        expected = self.render_location(location)
        self.assertDictEqual(actual, expected)

        self.assertEqual(location.battery_level, 0.8)
        self.assertEqual(location.is_gps_enabled, True)
        self.assertEqual(location.is_location_service_enabled, True)
        self.assertEqual(location.location_access_status, "ALLOWED_WHEN_IN_USE")

    def test_create_location__without_an_existing_assigned_location(self) -> None:
        location = self.create_location(is_assigned=False)
        data = {
            "latitude": -1.9353231,
            "longitude": 30.0692974,
            "batteryLevel": 0.8,
            "isGpsEnabled": True,
            "isLocationServiceEnabled": True,
            "locationAccessStatus": "ALLOWED_WHEN_IN_USE",
        }

        url = reverse(
            "driver-locations",
            args=(location.driver.driver_id,),
        )
        response = self.client.post(url, data=data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(Location.objects.count(), 1)

        location.refresh_from_db()
        actual = json.loads(response.content)
        expected = self.render_location(location)
        self.assertDictEqual(actual, expected)

        self.assertEqual(location.battery_level, 0.8)
        self.assertEqual(location.is_gps_enabled, True)
        self.assertEqual(location.is_location_service_enabled, True)
        self.assertEqual(location.location_access_status, "ALLOWED_WHEN_IN_USE")

    def test_get_all_delays(self) -> None:
        driver = self.create_driver()
        delays = [self.create_delay(driver=driver), self.create_delay()]

        url = reverse(
            "driver-delays",
            args=(driver.driver_id,),
        )
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_page(1, [self.render_delay(delays[0])])
        self.assertDictEqual(actual, expected)

    def test_get_all_current_assignments(self) -> None:
        package_ids = [
            "19b34722-f0af-42ba-bd02-0bc7131fae96",
            "23876d54-78a8-4757-9090-bf4567ad12f2",
        ]
        with requests_mock.Mocker() as mocker:
            mocker.get(
                f"http://order-api/packages?packageId={package_ids[1]},{package_ids[0]}",
                json={
                    "results": [
                        load_fixture("package1.json"),
                        load_fixture("package2.json"),
                    ]
                },
            )

            drivers = [self.create_driver(), self.create_driver()]
            assignments = [
                self.create_assignment(driver=drivers[0], package_id=package_ids[0]),
                self.create_assignment(driver=drivers[0], package_id=package_ids[1]),
                self.create_assignment(driver=drivers[1]),
            ]
            stops = [
                self.create_stop(driver=drivers[0], ranking=0),
                self.create_stop(driver=drivers[0], ranking=1),
                self.create_stop(driver=drivers[0], ranking=2),
                self.create_stop(driver=drivers[1], ranking=0),
                self.create_stop(driver=drivers[1], ranking=1),
            ]
            tasks = [
                self.create_task(
                    type=TASK_TYPE.PICK_UP, stop=stops[0], assignment=assignments[1]
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF, stop=stops[1], assignment=assignments[1]
                ),
                self.create_task(
                    type=TASK_TYPE.PICK_UP, stop=stops[1], assignment=assignments[0]
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF, stop=stops[2], assignment=assignments[0]
                ),
                self.create_task(
                    type=TASK_TYPE.PICK_UP, stop=stops[3], assignment=assignments[2]
                ),
                self.create_task(
                    type=TASK_TYPE.DROP_OFF, stop=stops[4], assignment=assignments[2]
                ),
            ]

            url = reverse(
                "driver-current-assignments",
                args=(drivers[0].driver_id,),
            )
            response = self.client.get(url)
            self.assertEqual(response.status_code, status.HTTP_200_OK)

            actual = json.loads(response.content)
            expected = self.render_page(
                2,
                [
                    self.render_current_assignment(assignments[1], "package2.json"),
                    self.render_current_assignment(assignments[0], "package1.json"),
                ],
            )
            self.assertDictEqual(actual, expected)

    def test_get_all_current_stops(self) -> None:
        with requests_mock.Mocker() as mocker:
            mocker.get(
                "http://order-api/packages?packageId=19b34722-f0af-42ba-bd02-0bc7131fae96",
                json={"results": [load_fixture("package1.json")]},
            )
            mocker.get(
                "http://order-api/packages?packageId=1e74abdf-9026-42ef-838b-31f6c9c8f2ad",
                json={"results": [load_fixture("package3.json")]},
            )

            drivers = [self.create_driver(), self.create_driver()]
            stops = [
                self.create_stop(driver=drivers[0], ranking=0),
                self.create_stop(driver=drivers[0], ranking=1),
                self.create_stop(driver=drivers[1], ranking=0),
                self.create_stop(driver=drivers[1], ranking=1),
            ]
            assignments = [
                self.create_confirmed_assignment(
                    driver=drivers[0], package_id="19b34722-f0af-42ba-bd02-0bc7131fae96"
                ),
                self.create_confirmed_assignment(
                    driver=drivers[1], package_id="1e74abdf-9026-42ef-838b-31f6c9c8f2ad"
                ),
            ]
            tasks = [
                self.create_task(
                    stop=stops[0], assignment=assignments[0], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[1], assignment=assignments[0], type=TASK_TYPE.DROP_OFF
                ),
                self.create_task(
                    stop=stops[2], assignment=assignments[1], type=TASK_TYPE.PICK_UP
                ),
                self.create_task(
                    stop=stops[3], assignment=assignments[1], type=TASK_TYPE.DROP_OFF
                ),
            ]

            url = reverse(
                "driver-current-stops",
                args=(drivers[0].driver_id,),
            )
            response = self.client.get(url)
            self.assertEqual(response.status_code, status.HTTP_200_OK)

            actual = json.loads(response.content)
            expected = self.render_page(
                2,
                [
                    self.render_current_stop(stops[0], "package1.json"),
                    self.render_current_stop(stops[1], "package1.json"),
                ],
            )
            self.assertDictEqual(actual, expected)


class DriverTrackingViewTestCaseV1_0(APITestCaseV1_0):
    def test_get_driver(self) -> None:
        driver = self.create_driver()

        url = reverse("driver-tracking-detail", args=(driver.driver_id,))
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        actual = json.loads(response.content)
        expected = self.render_driver(driver)
        self.assertDictEqual(actual, expected)
