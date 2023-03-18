package com.vanoma.api.order.maps;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum KigaliDistrict {
    NYARUGENGE, GASABO, KICUKIRO;

    public static KigaliDistrict create(String district) {
        if (district != null && !isValid(district)) {
            throw new InvalidParameterException("crud.address.district.invalid");
        } else if (district == null) {
            return null;
        } else {
            return KigaliDistrict.valueOf(district.toUpperCase());
        }
    }

    private static boolean isValid(String district) {
        if (district == null) return false;
        for (KigaliDistrict p : KigaliDistrict.values()) {
            if (district.toUpperCase().equals(p.name())) return true;
        }
        return false;
    }
}
