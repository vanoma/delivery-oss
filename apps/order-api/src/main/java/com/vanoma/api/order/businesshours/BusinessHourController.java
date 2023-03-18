package com.vanoma.api.order.businesshours;

import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import com.vanoma.api.order.utils.annotations.PutMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMappingJson
public class BusinessHourController {
    @Autowired
    private IBusinessHourService businessHourService;

    @PutMappingJson(value = "/business-hours")
    public ResponseEntity<BusinessHour> createOrUpdateBusinessHour(@RequestBody BusinessHourJson businessHourJson) {
        return this.businessHourService.createOrUpdate(businessHourJson);
    }

    @GetMapping(value = "/business-hours")
    public ResponseEntity<List<BusinessHour>> getBusinessHours() {
        return this.businessHourService.getBusinessHours();
    }
}
