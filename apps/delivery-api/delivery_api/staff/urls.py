from rest_framework import routers
from .views import StaffViewSet

router = routers.SimpleRouter(trailing_slash=False)
router.register(r"staff", StaffViewSet, basename="staff")

urlpatterns = router.urls
