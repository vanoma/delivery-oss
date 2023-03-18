package com.vanoma.api.order.packages;

import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.orders.CurrentTimeWrapper;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.exceptions.ResourceNotFoundException;
import com.vanoma.api.utils.input.TimeUtils;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.vanoma.api.order.packages.Constants.MINUTES_TO_PICK_UP_EARLY;

public class PackageUtils {
    private static void validatePackage(Package pkg) {
        if (pkg.getFromContact() == null)
            throw new InvalidParameterException("crud.package.fromContact.notFound");
        if (pkg.getToContact() == null)
            throw new InvalidParameterException("crud.package.toContact.notFound");
        if (pkg.getFromAddress() == null)
            throw new InvalidParameterException("crud.package.fromAddress.notFound");
        if (pkg.getToAddress() == null)
            throw new InvalidParameterException("crud.package.toAddress.notFound");
        if (pkg.getSize() == null)
            throw new InvalidParameterException("crud.package.size.notFound");
    }

    public static void validateDeliveryOrderPackages(List<Package> packages) {
        if (packages == null || packages.size() == 0) {
            throw new InvalidParameterException("crud.deliveryOrder.packages.required");
        }

        for (Package pkg : packages) {
            validatePackage(pkg);
        }
    }

    public static void validateDeliveryOrderPackages(List<Package> packages, List<Charge> charges) {
        if (packages == null || packages.size() == 0) {
            throw new InvalidParameterException("crud.package.required");
        }

        Map<String, Charge> chargeMap = charges.stream().collect(Collectors.toMap(Charge::getPackageId, c -> c));

        for (Package pkg : packages) {
            validatePackage(pkg);

            if (!chargeMap.containsKey(pkg.getPackageId())) {
                throw new ResourceNotFoundException("crud.charge.deliveryFee.notFound");
            }
        }
    }

    public static OffsetDateTime getPickUpStart(CurrentTimeWrapper currentTimeWrapper, OffsetDateTime pickUpStart) {
        if (pickUpStart != null) return pickUpStart;

        OffsetTime currentTime = currentTimeWrapper.getCurrentTimeUtc();
        OffsetDateTime utcNow = TimeUtils.getUtcNow()
                .withHour(currentTime.getHour())
                .withMinute(currentTime.getMinute());
        return utcNow.plusMinutes(MINUTES_TO_PICK_UP_EARLY);
    }
}
