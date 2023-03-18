package com.vanoma.api.order.packages;

import com.vanoma.api.order.orders.DeliveryOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface IPackageService {
    Page<Package> getPackages(PackageFilter filter, Pageable pageable);

    Package createPackage(String deliveryOrderId, PackageJson packageJson);

    Package createPackage(DeliveryOrder order, PackageJson json, PackageStatus status);

    Package updatePackage(String packageId, PackageJson packageJson, String authHeader);

    Package getPackageByTrackingNumber(String trackingNumber);

    void deletePackage(String packageId);

    void cancelPackage(String packageId, CancelPackageJson json);

    void cancelPackage(Package pkg, String reason);

    Package duplicatePackage(Package oldPackage, DeliveryOrder newOrder, OffsetDateTime pickUpStart);
}
