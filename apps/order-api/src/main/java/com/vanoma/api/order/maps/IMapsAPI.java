package com.vanoma.api.order.maps;

import com.vanoma.api.utils.httpwrapper.HttpResult;

public interface IMapsAPI {

    HttpResult geocode(AddressLine addressLine);

    HttpResult reverseGeocode(Coordinates coordinates);
}
