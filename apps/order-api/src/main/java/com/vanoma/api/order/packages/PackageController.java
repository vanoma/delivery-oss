package com.vanoma.api.order.packages;

import com.vanoma.api.utils.PagedResources;
import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import com.vanoma.api.order.utils.annotations.PatchMappingJson;
import com.vanoma.api.order.utils.annotations.PostMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMappingJson
public class PackageController {

    @Autowired
    private IPackageService packageService;
    @Autowired
    private PackageRepository packageRepository;

    @GetMapping(value = "/packages")
    public ResponseEntity<PagedResources<Package>> getPackages(PackageFilter filter,
                                                               Pageable pageable) {
        return ResponseEntity.ok(PagedResources.of(this.packageService.getPackages(filter, pageable)));
    }

    @GetMapping(value = "/customers/{customerId}/packages")
    public ResponseEntity<PagedResources<Package>> getCustomerPackages(@PathVariable String customerId,
                                                                       PackageFilter filter,
                                                                       Pageable pageable) {
        filter.setCustomerId(customerId);
        return ResponseEntity.ok(PagedResources.of(this.packageService.getPackages(filter, pageable)));
    }

    @GetMapping(value = "/delivery-orders/{deliveryOrderId}/packages")
    public ResponseEntity<PagedResources<Package>> getDeliveryOrderPackages(@PathVariable String deliveryOrderId,
                                                                            Pageable pageable) {
        PackageFilter filter = PackageFilter.builder().deliveryOrderId(deliveryOrderId).build();
        return ResponseEntity.ok(PagedResources.of(this.packageService.getPackages(filter, pageable)));
    }

    @PostMappingJson(value = "/delivery-orders/{deliveryOrderId}/packages")
    public ResponseEntity<Package> createPackage(@PathVariable String deliveryOrderId,
                                                 @RequestBody PackageJson packageJson) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.packageService.createPackage(deliveryOrderId, packageJson));
    }

    @PatchMappingJson(path = "/packages/{packageId}")
    public ResponseEntity<Package> updatePackage(@PathVariable String packageId,
                                                 @RequestBody PackageJson packageJson,
                                                 @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(this.packageService.updatePackage(packageId, packageJson, authHeader));
    }

    @GetMapping(value = "/packages/{packageId}")
    public ResponseEntity<Package> getPackageByPackageId(@PathVariable String packageId) {
        return ResponseEntity.ok(this.packageRepository.getById(packageId));
    }

    @DeleteMapping(value = "/packages/{packageId}")
    public ResponseEntity<Void> deletePackage(@PathVariable String packageId) {
        this.packageService.deletePackage(packageId);
        return ResponseEntity.noContent().build();
    }

    @PostMappingJson(value = "/packages/{packageId}/cancellation")
    public ResponseEntity<Void> cancelPackage(@PathVariable String packageId,
                                              @RequestBody CancelPackageJson cancelPackageJson) {
        this.packageService.cancelPackage(packageId, cancelPackageJson);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/package-tracking/{trackingNumber}")
    public ResponseEntity<Package> getPackageByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(this.packageService.getPackageByTrackingNumber(trackingNumber));
    }
}
