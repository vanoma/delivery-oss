from typing import Any, Type
from rest_framework import mixins
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.request import Request
from rest_framework.serializers import Serializer, BaseSerializer
from vanoma_api_utils.rest_framework import viewsets, generics
from delivery_api.deliveries.views import (
    DelayViewSet,
    CurrentStopViewSet,
    CurrentAssignmentViewSet,
)
from .models import Driver, Location
from .serializers import DriverSerializerV1_0, LocationSerializerV1_0
from .filters import DriverFilter


class DriverViewSet(
    viewsets.GenericViewSet,
    mixins.ListModelMixin,
    mixins.UpdateModelMixin,
    mixins.RetrieveModelMixin,
    mixins.CreateModelMixin,
):
    filterset_class = DriverFilter
    queryset = Driver.objects.all()

    @action(detail=True, methods=["POST"])
    def locations(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        return LocationViewSet.as_view({"post": "create"})(
            request._request, args, **{"driver": self.get_object(), **kwargs}
        )

    @action(detail=True, methods=["GET"])
    def delays(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        return DelayViewSet.as_view({"get": "list"})(
            request._request, args, **{"driver": self.get_object(), **kwargs}
        )

    @action(detail=True, methods=["GET"], url_path="current-assignments")
    def current_assignments(
        self, request: Request, *args: Any, **kwargs: Any
    ) -> Response:
        return CurrentAssignmentViewSet.as_view({"get": "list"})(
            request._request, args, **{"driver": self.get_object(), **kwargs}
        )

    @action(detail=True, methods=["GET"], url_path="current-stops")
    def current_stops(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        return CurrentStopViewSet.as_view({"get": "list"})(
            request._request, args, **{"driver": self.get_object(), **kwargs}
        )

    def get_serializer_class_v1_0(self, *args: Any, **kwargs: Any) -> Type[Serializer]:
        return DriverSerializerV1_0

    def get_object(self) -> Driver:
        if self.detail is None:
            return super().get_object()

        # Detail routes could have query parameters targeting the detail route (e.g. filtering driver
        # assignments by status), and the default implementation of get_object() always filters drivers
        # with such query parametes. And that's not the desired behavior.
        return Driver.objects.get(driver_id=self.kwargs["pk"])


class LocationViewSet(
    viewsets.GenericViewSet,
    mixins.CreateModelMixin,
):
    queryset = Location.objects.all()

    def perform_create(self, serializer: BaseSerializer) -> None:
        serializer.save(driver=self.kwargs["driver"])

    def get_serializer_class_v1_0(self, *args: Any, **kwargs: Any) -> Type[Serializer]:
        return LocationSerializerV1_0


class DriverTrackingView(generics.GenericAPIView):
    def get(self, *args: Any, **kwargs: Any) -> Response:
        driver = Driver.objects.get(driver_id=self.kwargs["driver_id"])
        serializer = self.get_serializer(driver)
        return Response(serializer.data)

    def get_serializer_class_v1_0(self, *args: Any, **kwargs: Any) -> Type[Serializer]:
        return DriverSerializerV1_0
