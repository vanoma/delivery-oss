package com.vanoma.api.order.utils;

import java.time.OffsetDateTime;

public class Dates {
    public static String stringifyDatetime(OffsetDateTime value) {
        return stringifyDatetime(value.toString());
    }

    private static String stringifyDatetime(String value) {
        return value.endsWith("0Z") ? stringifyDatetime(value.replace("0Z", "Z")) : value;
    }
}
