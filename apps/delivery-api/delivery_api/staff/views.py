from typing import Any, Type
from rest_framework import mixins
from rest_framework.serializers import Serializer
from vanoma_api_utils.rest_framework import viewsets
from .models import Staff
from .serializers import StaffSerializerV1_0


class StaffViewSet(
    viewsets.GenericViewSet,
    mixins.ListModelMixin,
    mixins.UpdateModelMixin,
    mixins.RetrieveModelMixin,
    mixins.CreateModelMixin,
):
    queryset = Staff.objects.all()

    def get_serializer_class_v1_0(self, *args: Any, **kwargs: Any) -> Type[Serializer]:
        return StaffSerializerV1_0
