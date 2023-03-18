package com.vanoma.api.order.charges;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum ChargeStatus {
    UNPAID, PAID;

    public static ChargeStatus create(String chargeStatus) {
        if (chargeStatus == null) {
            return null;
        } else if (!isValid(chargeStatus)) {
            throw new InvalidParameterException("crud.charge.status.invalid");
        } else {
            return ChargeStatus.valueOf(chargeStatus);
        }
    }

    private static boolean isValid(String status) {
        if (status == null) return false;
        for (ChargeStatus s : ChargeStatus.values()) {
            if (status.equals(s.name())) return true;
        }
        return false;
    }
}
