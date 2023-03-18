package com.vanoma.api.order.payment;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum PaymentStatus {
    NO_CHARGE, UNPAID, PARTIAL, PAID;

    public static PaymentStatus create(String paymentStatus) {
        if (paymentStatus == null) {
            return null;
        } else if (!isValid(paymentStatus)) {
            throw new InvalidParameterException("crud.deliveryOrder.paymentStatus.invalid");
        } else {
            return PaymentStatus.valueOf(paymentStatus);
        }
    }

    private static boolean isValid(String status) {
        if (status == null) return false;
        for (PaymentStatus s : PaymentStatus.values()) {
            if (status.equals(s.name())) return true;
        }
        return false;
    }
}
