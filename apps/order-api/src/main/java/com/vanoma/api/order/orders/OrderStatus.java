package com.vanoma.api.order.orders;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum OrderStatus {
    // Transitional statuses.
    REQUEST,
    STARTED,
    PENDING,
    PLACED,

    // TODO: Do we need these statuses? Kept them here to keep them in sync with package statuses. Also might be a useful if we ever show orders to users.
    // Final statuses.
    COMPLETE,
    CANCELED,
    INCOMPLETE;

    public static OrderStatus create(String status) {
        if (status == null) {
            return null;
        } else if (!isValid(status)) {
            throw new InvalidParameterException("crud.deliveryOrder.status.invalid");
        } else {
            return OrderStatus.valueOf(status);
        }
    }

    private static boolean isValid(String status) {
        if (status == null) return false;
        for (OrderStatus s : OrderStatus.values()) {
            if (status.equals(s.name())) return true;
        }
        return false;
    }
}
