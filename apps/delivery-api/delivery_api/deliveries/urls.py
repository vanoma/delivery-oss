from django.urls import path
from rest_framework import routers
from .views import (
    DelayViewSet,
    AssignmentViewSet,
    CurrentStopViewSet,
    CurrentAssignmentViewSet,
    CurrentTaskCompletionView,
    AssignmentConfirmationView,
)

router = routers.SimpleRouter(trailing_slash=False)

router.register(
    r"driver-delays",
    DelayViewSet,
    basename="delay",
)
router.register(
    r"assignments",
    AssignmentViewSet,
    basename="assignment",
)
router.register(
    r"current-assignments",
    CurrentAssignmentViewSet,
    basename="current-assignment",
)
router.register(
    r"current-stops",
    CurrentStopViewSet,
    basename="current-stop",
)

urlpatterns = [
    path(
        "current-tasks/<task_id>/completion",
        CurrentTaskCompletionView.as_view(),
        name="current-task-completion",
    ),
    path(
        "assignment-confirmations",
        AssignmentConfirmationView.as_view(),
        name="assignment-confirmation-list",
    ),
] + router.urls
