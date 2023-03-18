package com.vanoma.api.order.packages;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum PackageStatus {
    // Transitional statuses.
    REQUEST,
    STARTED,
    PENDING,
    PLACED,

    // TODO: Should order have these statuses as well? Might be a good if we ever show orders to users.
    // Final statuses.
    COMPLETE,
    CANCELED,
    INCOMPLETE,

    // TODO: Delete this status once driver-api is in production. This status is no longer used.
    ASSIGNED;


    public static PackageStatus create(String status) {
        if (status == null) {
            return null;
        } else if (!isValid(status)) {
            throw new InvalidParameterException("crud.packages.status.invalid");
        } else {
            return PackageStatus.valueOf(status);
        }
    }

    private static boolean isValid(String status) {
        if (status == null) return false;
        for (PackageStatus s : PackageStatus.values()) {
            if (status.equals(s.name())) return true;
        }
        return false;
    }
}
