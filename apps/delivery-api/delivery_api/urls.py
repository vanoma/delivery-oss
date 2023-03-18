from django.urls import path
from delivery_api.staff import urls as staff_urls
from delivery_api.drivers import urls as drivers_urls
from delivery_api.deliveries import urls as deliveries_urls
from vanoma_api_utils.django.views import create_root_view


urlpatterns = (
    [
        path("", create_root_view("driver")),
    ]
    + staff_urls.urlpatterns
    + drivers_urls.urlpatterns
    + deliveries_urls.urlpatterns
)
