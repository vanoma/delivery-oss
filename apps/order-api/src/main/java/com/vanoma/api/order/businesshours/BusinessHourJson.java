package com.vanoma.api.order.businesshours;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.io.Serializable;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;

public class BusinessHourJson implements Serializable {

    private static int MIN_OPEN_HOURS = 4;

    private Integer weekDay;
    private Boolean isDayOff;
    private String openAt;
    private String closeAt;

    public BusinessHourJson() {
    }

    public Integer getWeekDay() {
        return weekDay;
    }

    public Boolean getIsDayOff() {
        return isDayOff;
    }

    public OffsetTime getOpenAt() {
        return parseTimeString(openAt);
    }

    public OffsetTime getCloseAt() {
        return parseTimeString(closeAt);
    }

    // Setters mostly for testing

    public BusinessHourJson setWeekDay(Integer weekDay) {
        this.weekDay = weekDay;
        return this;
    }

    public BusinessHourJson setIsDayOff(Boolean holiday) {
        isDayOff = holiday;
        return this;
    }

    public BusinessHourJson setOpenAt(String openAt) {
        this.openAt = openAt;
        return this;
    }

    public BusinessHourJson setCloseAt(String closeAt) {
        this.closeAt = closeAt;
        return this;
    }

    public void validate() {
        if (weekDay == null) {
            throw new InvalidParameterException("crud.businessHour.weekDay.required");
        }
        if (isDayOff()) {
            validateDayOff();
        } else {
            validateWorkDays();
        }
    }

    private void validateDayOff() {
        if (openAt != null) {
            throw new InvalidParameterException("crud.businessHour.noOpenAtForDayOff");
        } else if (closeAt != null) {
            throw new InvalidParameterException("crud.businessHour.noCloseAtForDayOff");
        }
    }

    private void validateWorkDays() {
        if (openAt == null) {
            throw new InvalidParameterException("crud.businessHour.openAt.required");
        } else if (closeAt == null) {
            throw new InvalidParameterException("crud.businessHour.closeAt.required");
        } else if (getOpenAt().isAfter(getCloseAt())) {
            throw new InvalidParameterException("crud.businessHour.openAtAfterCloseAt");
        } else if (getCloseAt().getHour() - getOpenAt().getHour() < MIN_OPEN_HOURS) {
            throw new InvalidParameterException("crud.businessHour.insufficientInterval");
        }
    }

    private boolean isDayOff() {
        return isDayOff != null && isDayOff;
    }

    public static OffsetTime parseTimeString(String value) {
        if (value == null) return null;
        if (!value.endsWith("Z")) {
            throw new java.security.InvalidParameterException("api.utils.utcTimeRequired");
        }
        OffsetTime time;
        try {
            time = OffsetTime.parse(value);
        } catch (DateTimeParseException err) {
            throw new java.security.InvalidParameterException("api.utils.invalidTimeInput");
        }
        return time;
    }
}
