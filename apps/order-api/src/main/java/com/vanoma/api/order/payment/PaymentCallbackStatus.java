package com.vanoma.api.order.payment;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum PaymentCallbackStatus {
    FAILURE, SUCCESS;

    public static PaymentCallbackStatus create(String status) {
        if (status != null && !isValid(status)) {
            throw new InvalidParameterException("crud.paymentAttempt.callback.status.invalid");
        } else if (status == null) {
            return null;
        } else {
            return PaymentCallbackStatus.valueOf(status);
        }
    }

    private static boolean isValid(String status) {
        if (status == null) return false;
        for (PaymentCallbackStatus p : PaymentCallbackStatus.values()) {
            if (status.equals(p.name())) return true;
        }
        return false;
    }
}
