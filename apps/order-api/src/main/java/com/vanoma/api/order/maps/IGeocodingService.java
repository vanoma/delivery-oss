package com.vanoma.api.order.maps;

import com.vanoma.api.utils.httpwrapper.HttpResult;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface IGeocodingService {

    ResponseEntity<Map<String, Object>> geocode(GeocodeRequestJson json);

    HttpResult geocode(AddressLine addressLine);

    ResponseEntity<Map<String, Object>> reverseGeocode(ReverseGeocodeRequestJson json);

    HttpResult reverseGeocode(Coordinates coordinates);
}
