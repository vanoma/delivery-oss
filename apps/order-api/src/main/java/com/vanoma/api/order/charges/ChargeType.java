package com.vanoma.api.order.charges;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum ChargeType {
    DELIVERY_FEE, PICK_UP_DELAY, PHONE_ORDER, EXTRA_DISTANCE;

    public static ChargeType create(String type) {
        if (type == null) {
            return null;
        } else if (!isValid(type)) {
            throw new InvalidParameterException("crud.charge.type.invalid");
        } else {
            return ChargeType.valueOf(type);
        }
    }

    private static boolean isValid(String type) {
        if (type == null) return false;
        for (ChargeType s : ChargeType.values()) {
            if (type.equals(s.name())) return true;
        }
        return false;
    }
}
