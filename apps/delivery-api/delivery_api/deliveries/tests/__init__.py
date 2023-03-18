from delivery_api.drivers.tests import (
    ModelsMixin as DriversModelsMixin,
    APITestCaseV1_0 as DriversAPITestCaseV1_0,
)


class ModelsMixin(DriversModelsMixin):
    pass


class APITestCaseV1_0(ModelsMixin, DriversAPITestCaseV1_0):
    pass
