package com.vanoma.api.order.packages;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum PackageSize {
    SMALL, MEDIUM, LARGE;

    private static String ERROR_MESSAGE = "Use valid package size (SMALL, MEDIUM, or LARGE)";

    public static PackageSize create(String size) {
        if (size == null) {
            return null;
        } else if (!isValid(size)) {
            throw new InvalidParameterException(ERROR_MESSAGE);
        } else {
            return PackageSize.valueOf(size);
        }
    }

    private static boolean isValid(String size) {
        if (size == null) return false;
        for (PackageSize s : PackageSize.values()) {
            if (size.equals(s.name())) return true;
        }
        return false;
    }
}
