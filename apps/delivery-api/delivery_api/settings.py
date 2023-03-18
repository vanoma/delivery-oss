import os
from pathlib import Path
from vanoma_api_utils.django.settings import (
    resolve_database,
    resolve_logging,
    resolve_file_storage,
)
from vanoma_api_utils.misc import resolve_debug, resolve_environment


# Application variables

ENVIRONMENT = resolve_environment()

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = os.environ.get("SECRET_KEY")

DEBUG = resolve_debug()

ALLOWED_HOSTS = ["*"]


# Application definition

INSTALLED_APPS = [
    "rest_framework",
    "corsheaders",
    "django_filters",
    "delivery_api.staff",
    "delivery_api.drivers",
    "delivery_api.deliveries",
    "delivery_api.mini_huey",
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.middleware.common.CommonMiddleware",
    "vanoma_api_utils.django.middlewares.CamelCaseMiddleWare",
]

ROOT_URLCONF = "delivery_api.urls"

WSGI_APPLICATION = "delivery_api.wsgi.application"

DATABASES = {"default": resolve_database()}

LANGUAGE_CODE = "en-us"

TIME_ZONE = "UTC"

USE_I18N = True

USE_L10N = True

USE_TZ = True

LOGGING = resolve_logging()

CACHES = {
    "default": {
        "BACKEND": "django.core.cache.backends.filebased.FileBasedCache",
        "LOCATION": "/var/tmp/django_cache",
    }
}


# Rest Framework

REST_FRAMEWORK = {
    # "DEFAULT_AUTHENTICATION_CLASSES": (
    #     "rest_framework_simplejwt.authentication.JWTAuthentication",
    # ),
    # "DEFAULT_PERMISSION_CLASSES": ("rest_framework.permissions.IsAuthenticated",),
    "UNAUTHENTICATED_USER": None,
    "DEFAULT_FILTER_BACKENDS": (
        "django_filters.rest_framework.DjangoFilterBackend",
        "rest_framework.filters.OrderingFilter",
    ),
    "DEFAULT_RENDERER_CLASSES": (
        "vanoma_api_utils.rest_framework.renderers.VanomaMediaTypeRenderer",
    ),
    "DEFAULT_PARSER_CLASSES": (
        "vanoma_api_utils.rest_framework.parsers.VanomaMediaTypeParser",
    ),
    "TEST_REQUEST_DEFAULT_FORMAT": "json",
    "EXCEPTION_HANDLER": "vanoma_api_utils.rest_framework.views.exception_handler",
    "DEFAULT_VERSIONING_CLASS": "rest_framework.versioning.AcceptHeaderVersioning",
    "DEFAULT_PAGINATION_CLASS": "rest_framework.pagination.LimitOffsetPagination",
    "PAGE_SIZE": 100,
    "ORDERING_PARAM": "sort",
}


# Django storages

DEFAULT_FILE_STORAGE = resolve_file_storage()
AWS_ACCESS_KEY_ID = os.environ.get("AWS_ACCESS_KEY_ID")
AWS_SECRET_ACCESS_KEY = os.environ.get("AWS_SECRET_ACCESS_KEY")
AWS_STORAGE_BUCKET_NAME = os.environ.get("AWS_STORAGE_BUCKET_NAME")
AWS_QUERYSTRING_AUTH = False
AWS_DEFAULT_ACL = None


# Google Maps

GOOGLE_MAPS_API_KEY = os.environ.get(
    "GOOGLE_MAPS_API_KEY", "AIza"
)  # Using a default value to avoid google maps throwing exceptoins during testing.


# Web Push

WEB_PUSH_DRIVER_APP_ID = os.environ.get("WEB_PUSH_DRIVER_APP_ID")
WEB_PUSH_DRIVER_API_KEY = os.environ.get("WEB_PUSH_DRIVER_API_KEY")
WEB_PUSH_NEW_ASSIGNMENT_CHANNEL_ID = os.environ.get(
    "WEB_PUSH_NEW_ASSIGNMENT_CHANNEL_ID"
)
WEB_PUSH_CANCELLED_ASSIGNMENT_CHANNEL_ID = os.environ.get(
    "WEB_PUSH_CANCELLED_ASSIGNMENT_CHANNEL_ID"
)


# Vanoma

VANOMA_AUTH_API_URL = os.environ.get("VANOMA_AUTH_API_URL")
VANOMA_ORDER_API_URL = os.environ.get("VANOMA_ORDER_API_URL")
VANOMA_COMMUNICATION_API_URL = os.environ.get("VANOMA_COMMUNICATION_API_URL")
