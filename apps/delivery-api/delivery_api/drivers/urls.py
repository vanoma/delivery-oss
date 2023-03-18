from django.urls import path
from rest_framework import routers
from .views import DriverViewSet, DriverTrackingView

router = routers.SimpleRouter(trailing_slash=False)
router.register(r"drivers2", DriverViewSet, basename="driver")

urlpatterns = [
    path(
        "driver-tracking/<driver_id>",
        DriverTrackingView.as_view(),
        name="driver-tracking-detail",
    ),
] + router.urls
