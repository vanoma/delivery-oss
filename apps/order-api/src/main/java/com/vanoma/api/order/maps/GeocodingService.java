package com.vanoma.api.order.maps;

import com.vanoma.api.utils.httpwrapper.HttpResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GeocodingService implements IGeocodingService {
    private IMapsAPI mapsClient;

    public GeocodingService(IMapsAPI mapsClient) {
        this.mapsClient = mapsClient;
    }

    @Override
    public ResponseEntity<Map<String, Object>> geocode(GeocodeRequestJson json) {
        AddressLine addressLine = new AddressLine()
                .setHouseNumber(json.getHouseNumber())
                .setStreetName(json.getStreetName());
        HttpResult httpResult = this.geocode(addressLine);
        return new ResponseEntity<>(httpResult.getBody(), httpResult.getHttpStatus());
    }

    @Override
    public HttpResult geocode(AddressLine addressLine) {
        return this.mapsClient.geocode(addressLine);
    }

    @Override
    public ResponseEntity<Map<String, Object>> reverseGeocode(ReverseGeocodeRequestJson json) {
        Coordinates coordinates = new Coordinates()
                .setLat(json.getLatitude())
                .setLng(json.getLongitude());
        HttpResult httpResult = this.reverseGeocode(coordinates);
        return new ResponseEntity<>(httpResult.getBody(), httpResult.getHttpStatus());
    }

    @Override
    public HttpResult reverseGeocode(Coordinates coordinates) {
        return this.mapsClient.reverseGeocode(coordinates);
    }
}
