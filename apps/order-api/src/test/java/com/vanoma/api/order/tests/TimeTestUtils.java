package com.vanoma.api.order.tests;

import com.vanoma.api.utils.input.TimeUtils;

public class TimeTestUtils {
    public static int getDayOfWeek() {
        return TimeUtils.getUtcNow().getDayOfWeek().getValue();
    }
}
