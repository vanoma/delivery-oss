package com.vanoma.api.order.maps;

import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import com.vanoma.api.order.utils.annotations.PostMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMappingJson("/maps")
public class MapsController {

    @Autowired
    private IGeocodingService geocodingService;

    @PostMappingJson(value = "/geocode")
    public ResponseEntity<Map<String, Object>> geocode(@RequestBody GeocodeRequestJson json) {
        return this.geocodingService.geocode(json);
    }

    @PostMappingJson(value = "/reverse-geocode")
    public ResponseEntity<Map<String, Object>> reverseGeocode(@RequestBody ReverseGeocodeRequestJson json) {
        return this.geocodingService.reverseGeocode(json);
    }
}
