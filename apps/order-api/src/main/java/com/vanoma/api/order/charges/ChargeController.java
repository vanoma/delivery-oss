package com.vanoma.api.order.charges;

import com.vanoma.api.utils.PagedResources;
import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import com.vanoma.api.order.utils.annotations.PostMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMappingJson
public class ChargeController {
    @Autowired
    private IChargeService chargeService;
    @Autowired
    private ChargeRepository chargeRepository;

    @PostMappingJson(value = "/packages/{packageId}/charges")
    public ResponseEntity<Charge> createCharge(@PathVariable String packageId,
                                               @RequestBody ChargeJson chargeJson) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.chargeService.createCharge(packageId, chargeJson));
    }

    @GetMapping(value = "/packages/{packageId}/charges")
    public ResponseEntity<PagedResources<Charge>> getCharges(@PathVariable String packageId,
                                                             Pageable pageable) {
        ChargeFilter filter = ChargeFilter.builder().packageId(packageId).build();
        return ResponseEntity.ok(this.chargeService.getCharges(filter, pageable));
    }
}
