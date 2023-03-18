from django_filters import rest_framework as filters  # type: ignore
from vanoma_api_utils.rest_framework.filters import CharInFilter
from .models import Assignment, Delay


class DelayFilter(filters.FilterSet):
    start_at = filters.DateFilter(field_name="created_at", lookup_expr="gte")
    end_at = filters.DateFilter(field_name="created_at", lookup_expr="lte")

    class Meta:
        model = Delay
        fields = ["status", "type", "start_at", "end_at"]


class AssignmentFilter(filters.FilterSet):
    status = CharInFilter(field_name="status", lookup_expr="in")
    assignment_id = CharInFilter(field_name="assignment_id", lookup_expr="in")
    package_id = CharInFilter(field_name="package_id", lookup_expr="in")

    class Meta:
        model = Assignment
        exclude = ["status", "assignment_id"]
