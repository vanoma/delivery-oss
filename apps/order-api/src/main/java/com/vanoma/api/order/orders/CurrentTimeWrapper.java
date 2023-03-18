package com.vanoma.api.order.orders;

import com.vanoma.api.utils.input.TimeUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

@Service
public class CurrentTimeWrapper {

    public OffsetTime getCurrentTimeUtc() {
        return OffsetTime.now(Clock.systemUTC());
    }

    public OffsetDateTime getCurrentDateTimeUtc() {
        return TimeUtils.getUtcNow();
    }
}
