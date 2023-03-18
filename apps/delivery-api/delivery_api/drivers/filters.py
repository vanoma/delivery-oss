from django_filters import rest_framework as filters  # type: ignore
from vanoma_api_utils.rest_framework.filters import CharInFilter
from .models import Driver


class DriverFilter(filters.FilterSet):
    status = CharInFilter(field_name="status", lookup_expr="in")

    class Meta:
        model = Driver
        exclude = ["status"]
