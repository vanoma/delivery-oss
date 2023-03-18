from typing import Any, Type
from django.db.models import Q
from django.db.models.query import QuerySet
from django.core.cache import cache
from django.utils.translation import gettext_lazy as _
from itertools import chain
from rest_framework import mixins, status
from rest_framework.serializers import Serializer
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.request import Request
from vanoma_api_utils.rest_framework import viewsets, generics
from vanoma_api_utils.rest_framework.responses import generic_error
from vanoma_api_utils.constants import ERROR_CODE
from delivery_api.deliveries.utils import order_api
from delivery_api.deliveries.utils.constants import (
    ASSIGNMENT_STATUS,
    ASSIGNMENT_TYPE,
    TASK_TYPE,
)
from delivery_api.drivers.models import Driver
from .models import Assignment, Delay, Stop, Task
from .utils.constants import CACHE_KEY
from .filters import DelayFilter, AssignmentFilter
from .serializers import (
    DelaySerializerV1_0,
    AssignmentSerializerV1_0,
    CurrentStopSerializerV1_0,
    CurrentAssignmentSerializerV1_0,
)
from .workflows import (
    confirm_assignments,
    invalidate_assignments,
    get_current_stops,
    depart_to_stop,
    arrive_to_stop,
    complete_task,
    create_assignments,
    send_cancelled_assignment_notification,
)


class DelayViewSet(
    viewsets.GenericViewSet, mixins.ListModelMixin, mixins.UpdateModelMixin
):
    filterset_class = DelayFilter

    def get_queryset(self) -> QuerySet:
        if self.kwargs.get("driver") is None:
            return Delay.objects.all()

        return Delay.objects.filter(driver=self.kwargs["driver"])

    def get_serializer_class_v1_0(self, *args: Any, **kwargs: Any) -> Type[Serializer]:
        return DelaySerializerV1_0


class AssignmentViewSet(
    viewsets.GenericViewSet, mixins.ListModelMixin, mixins.CreateModelMixin
):
    filterset_class = AssignmentFilter
    queryset = Assignment.objects.all()

    def create(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        existing_assignment_qs = Assignment.objects.filter(
            Q(status=ASSIGNMENT_STATUS.PENDING) | Q(status=ASSIGNMENT_STATUS.CONFIRMED),
            package_id=request.data["package_id"],
        )

        if existing_assignment_qs.exists():
            return generic_error(
                status.HTTP_400_BAD_REQUEST,
                ERROR_CODE.INVALID_REQUEST,
                _("Package is already assigned. Cancel existing assignment first."),
            )

        is_auto_assignment_running = cache.get(
            CACHE_KEY.IS_AUTO_ASSIGNMENT_RUNNING, False
        )
        if is_auto_assignment_running:
            return generic_error(
                status.HTTP_400_BAD_REQUEST,
                ERROR_CODE.INVALID_REQUEST,
                _(
                    "Automatic assignment task is running. Please wait a few seconds for the task to complete."
                ),
            )

        driver = Driver.objects.get(driver_id=request.data["driver_id"])
        assignments = create_assignments(
            driver, ASSIGNMENT_TYPE.MANUAL, [request.data["package_id"]]
        )
        serializer = self.get_serializer(assignments[0])
        return Response(data=serializer.data, status=status.HTTP_201_CREATED)

    def get_serializer_class_v1_0(self, *args: Any, **kwargs: Any) -> Type[Serializer]:
        return AssignmentSerializerV1_0


class CurrentAssignmentViewSet(
    viewsets.GenericViewSet, mixins.ListModelMixin, mixins.CreateModelMixin
):
    filterset_class = AssignmentFilter

    @action(detail=True, methods=["POST"])
    def cancellation(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        assignment = self.get_object()
        invalidate_assignments(ASSIGNMENT_STATUS.CANCELED, [assignment])
        send_cancelled_assignment_notification(assignment.driver)
        return Response(status=status.HTTP_204_NO_CONTENT)

    def get_queryset(self) -> QuerySet:
        # Filter by pickup tasks type so we can use only the pickup stop's ranking to sort assignments.
        queryset = Assignment.objects.filter(tasks__type=TASK_TYPE.PICK_UP)

        if self.kwargs.get("driver"):
            return queryset.filter(driver=self.kwargs.get("driver")).order_by(
                "tasks__stop__ranking"
            )

        return queryset.order_by("driver__created_at", "tasks__stop__ranking")

    def get_serializer(self, *args: Any, **kwargs: Any) -> Serializer:
        """
        Overriding this method so we can fetch packages from order-api before creating a serializer.
        """
        serializer_class = self.get_serializer_class()
        serializer_context = self.get_serializer_context()

        if len(args) > 0:
            if isinstance(args[0], list):
                filters = {"package_id": ",".join(map(lambda a: a.package_id, args[0]))}
            elif isinstance(args[0], Assignment):
                filters = {"package_id": args[0].package_id}
            else:
                raise Exception(_("Unexpected argument type"))

            serializer_context["packages"] = order_api.get_packages_dict(filters)

        kwargs.setdefault("context", serializer_context)
        return serializer_class(*args, **kwargs)

    def get_serializer_class_v1_0(self, *args: Any, **kwargs: Any) -> Type[Serializer]:
        return CurrentAssignmentSerializerV1_0


class CurrentStopViewSet(viewsets.GenericViewSet, mixins.ListModelMixin):
    @action(detail=True, methods=["POST"])
    def departure(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        depart_to_stop(self.get_object())
        return Response(status=status.HTTP_204_NO_CONTENT)

    @action(detail=True, methods=["POST"])
    def arrival(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        arrive_to_stop(self.get_object())
        return Response(status=status.HTTP_204_NO_CONTENT)

    def get_queryset(self) -> QuerySet:
        if self.kwargs.get("driver"):
            return get_current_stops(self.kwargs.get("driver")).order_by("ranking")

        return get_current_stops().order_by("driver__created_at", "ranking")

    def get_serializer(self, *args: Any, **kwargs: Any) -> Serializer:
        """
        Overriding this method so we can fetch packages from order-api before creating a serializer.
        """
        serializer_class = self.get_serializer_class()
        serializer_context = self.get_serializer_context()

        if len(args) > 0:
            if isinstance(args[0], list):
                filters = {
                    "package_id": ",".join(
                        sorted(
                            set(
                                chain.from_iterable(
                                    map(
                                        lambda s: s.tasks.filter(
                                            assignment__status=ASSIGNMENT_STATUS.CONFIRMED
                                        ).values_list(
                                            "assignment__package_id", flat=True
                                        ),
                                        args[0],
                                    )
                                )
                            )
                        )
                    )
                }
            elif isinstance(args[0], Stop):
                filters = {
                    "package_id": args[0]  # type: ignore
                    .tasks.filter(assignment__status=ASSIGNMENT_STATUS.CONFIRMED)
                    .values_list("assignment__package_id", flat=True)
                }
            else:
                raise Exception(_("Unexpected argument type"))

            serializer_context["packages"] = order_api.get_packages_dict(filters)

        kwargs.setdefault("context", serializer_context)
        return serializer_class(*args, **kwargs)

    def get_serializer_class_v1_0(self, *args: Any, **kwargs: Any) -> Type[Serializer]:
        return CurrentStopSerializerV1_0


class CurrentTaskCompletionView(generics.GenericAPIView):
    def post(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        task = Task.objects.filter(assignment__status=ASSIGNMENT_STATUS.CONFIRMED).get(
            task_id=kwargs["task_id"]
        )

        if task.completed_at:
            return generic_error(
                status.HTTP_400_BAD_REQUEST,
                ERROR_CODE.INVALID_REQUEST,
                _("Task is already completed"),
            )

        complete_task(task)
        return Response(status=status.HTTP_204_NO_CONTENT)


class AssignmentConfirmationView(generics.GenericAPIView):
    def post(self, request: Request, *args: Any, **kwargs: Any) -> Response:
        driver = Driver.objects.get(driver_id=request.data["driver_id"])

        if not driver.latest_location:
            return generic_error(
                status.HTTP_400_BAD_REQUEST,
                ERROR_CODE.INVALID_REQUEST,
                _("Driver does not have latest location"),
            )

        confirm_assignments(driver.latest_location, request.data["assignment_ids"])
        return Response(status=status.HTTP_204_NO_CONTENT)
