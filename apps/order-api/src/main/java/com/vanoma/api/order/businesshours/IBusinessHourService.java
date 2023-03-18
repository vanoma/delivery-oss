package com.vanoma.api.order.businesshours;

import com.vanoma.api.order.packages.Package;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IBusinessHourService {

    BusinessHour save(BusinessHour businessHour);

    BusinessHour findById(int weekDay);

    ResponseEntity<BusinessHour> createOrUpdate(BusinessHourJson json);

    void validateBusinessHours(List<Package> packages, String customerId);

    void validateBusinessHours(Package pkg, String customerId);

    ResponseEntity<List<BusinessHour>> getBusinessHours();
}
