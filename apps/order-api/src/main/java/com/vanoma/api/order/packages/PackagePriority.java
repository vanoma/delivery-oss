package com.vanoma.api.order.packages;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

public enum PackagePriority {
    NORMAL, EXPRESS;

    public static PackagePriority create(String priority) {
        if (priority != null && !isValid(priority)) {
            throw new InvalidParameterException("crud.package.priority.invalid");
        } else if (priority == null) {
            return null;
        } else {
            return PackagePriority.valueOf(priority);
        }
    }

    private static boolean isValid(String priority) {
        if (priority == null) return false;
        for (PackagePriority p : PackagePriority.values()) {
            if (priority.equals(p.name())) return true;
        }
        return false;
    }
}
