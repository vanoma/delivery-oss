package com.vanoma.api.order.businesshours;

import com.vanoma.api.utils.exceptions.InvalidParameterException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

@Entity
@Table(name = "business_hour")
public class BusinessHour {

    // Monday = 1, Sunday = 7
    @Id
    @Column(name = "week_day", nullable = false)
    private Integer weekDay;

    @Column(name = "is_day_off", nullable = true)
    private Boolean isDayOff = false;

    @Column(name = "open_at", nullable = true)
    private OffsetTime openAt;

    @Column(name = "close_at", nullable = true)
    private OffsetTime closeAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public BusinessHour() {
    }

    @PrePersist
    void prePersist() {
        if (isDayOff == null) this.isDayOff = false;
    }

    public Integer getWeekDay() {
        return weekDay;
    }

    public BusinessHour setWeekDay(Integer weekDay) {
        if (weekDay == null || weekDay < 1 || weekDay > 7) {
            throw new InvalidParameterException("crud.businessHour.weekDay.invalid");
        }
        this.weekDay = weekDay;
        return this;
    }

    public Boolean getIsDayOff() {
        return isDayOff;
    }

    public BusinessHour setIsDayOff(Boolean isDayOff) {
        this.isDayOff = isDayOff;
        return this;
    }

    public OffsetTime getOpenAt() {
        return openAt;
    }

    public BusinessHour setOpenAt(OffsetTime openAt) {

        this.openAt = openAt;
        return this;
    }

    public OffsetTime getCloseAt() {
        return closeAt;
    }

    public BusinessHour setCloseAt(OffsetTime closeAt) {
        this.closeAt = closeAt;
        return this;
    }
}
