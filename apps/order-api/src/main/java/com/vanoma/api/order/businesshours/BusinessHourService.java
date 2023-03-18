package com.vanoma.api.order.businesshours;

import com.vanoma.api.order.orders.CurrentTimeWrapper;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageUtils;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.exceptions.ResourceNotFoundException;
import com.vanoma.api.utils.input.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.List;

@Repository
public class BusinessHourService implements IBusinessHourService {

    @Autowired
    private BusinessHourRepository businessHourRepository;
    @Autowired
    private CurrentTimeWrapper currentTimeWrapper;

    @Override
    public BusinessHour save(BusinessHour businessHour) {
        return this.businessHourRepository.save(businessHour);
    }

    @Override
    public BusinessHour findById(int weekDay) {
        return this.businessHourRepository.findById(weekDay).orElse(null);
    }

    @Override
    public ResponseEntity<BusinessHour> createOrUpdate(BusinessHourJson json) {
        json.validate();
        int weekDay = json.getWeekDay();
        BusinessHour businessHour = new BusinessHour()
                .setWeekDay(weekDay);

        BusinessHour existingBusinessHour = this.findById(weekDay);
        if (existingBusinessHour != null) {
            businessHour = existingBusinessHour;
        }
        businessHour.setIsDayOff(json.getIsDayOff())
                .setOpenAt(json.getOpenAt())
                .setCloseAt(json.getCloseAt());
        BusinessHour saved = this.save(businessHour);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @Override
    public void validateBusinessHours(List<Package> packages, String customerId) {
        for (Package pkg : packages) {
            validateBusinessHours(pkg, customerId);
        }
    }

    @Override
    public void validateBusinessHours(Package pkg, String customerId) {
        // pickUpStart here can be null if for example user is using "soon enough" option in the UI.
        OffsetDateTime pickUpStart = PackageUtils.getPickUpStart(this.currentTimeWrapper, pkg.getPickUpStart());

        BusinessHour todayBusinessHours = getTodayBusinessHours();
        BusinessHour tomorrowBusinessHours = getTomorrowBusinessHours();
        if (todayBusinessHours.getIsDayOff()) {
            throw new InvalidParameterException("crud.businessHour.noOrderOnDayOff");
        }

        OffsetDateTime utcNow = getCurrentUtcTimeNow();
        if (pickUpStart.isBefore(utcNow)) {
            throw new InvalidParameterException("crud.package.pickUpStart.inThePast");
        }
        if (!isWithin48Hours(pickUpStart, utcNow)) {
            throw new InvalidParameterException("crud.package.pickUpStart.beyond48Hours");
        }

        this.validatePickUpWithinWorkHours(pickUpStart, todayBusinessHours, tomorrowBusinessHours, customerId);
    }

    private OffsetDateTime getCurrentUtcTimeNow() {
        OffsetTime currentTime = this.currentTimeWrapper.getCurrentTimeUtc(); // Used for testing
        return TimeUtils.getUtcNow()
                .withHour(currentTime.getHour())
                .withMinute(currentTime.getMinute());
    }

    private BusinessHour getTodayBusinessHours() {
        //TODO Need to account for differences between UTC and local time. 
        int weekDay = TimeUtils.getUtcNow().getDayOfWeek().getValue(); // between 1 & 7;
        BusinessHour todayBusinessHours = findById(weekDay);
        if (todayBusinessHours == null) throw new ResourceNotFoundException("crud.businessHour.notFound");
        return todayBusinessHours;
    }

    private BusinessHour getTomorrowBusinessHours() {
        //TODO Need to account for differences between UTC and local time.
        int weekDay = TimeUtils.getUtcNow().getDayOfWeek().getValue(); // between 1 & 7;
        weekDay = weekDay == 7 ? 1 : weekDay + 1;
        BusinessHour tomorrow = findById(weekDay);
        if (tomorrow == null && "production".equals(System.getenv("SPRING_PROFILES_ACTIVE"))) {
            throw new ResourceNotFoundException("crud.businessHour.notFound");
        }
        return tomorrow;
    }

    private void validatePickUpWithinWorkHours(OffsetDateTime pickUpStart, BusinessHour todayBusinessHours, BusinessHour tomorrowBusinessHour, String customerId) {
        List<String> lateDeliveryCustomers = Arrays.asList("eec3f1a97676471dba98a119aaeae63d", "ebfb2198f9f0479cba67c630705fdba6"); // Sawa Citi, Murukali

        int today = TimeUtils.getUtcNow().getDayOfWeek().getValue();
        boolean isToday = today == pickUpStart.getDayOfWeek().getValue();
        BusinessHour businessHour = isToday ? todayBusinessHours : tomorrowBusinessHour;
        OffsetDateTime openAt = isToday ?
                TimeUtils.getUtcNow()
                        .withHour(businessHour.getOpenAt().getHour())
                        .withMinute(businessHour.getOpenAt().getMinute()) :
                TimeUtils.getUtcNow().plusDays(1)
                        .withHour(businessHour.getOpenAt().getHour())
                        .withMinute(businessHour.getOpenAt().getMinute());

        OffsetDateTime closeAt = isToday ?
                TimeUtils.getUtcNow()
                        .withHour(businessHour.getCloseAt().getHour())
                        .withMinute(businessHour.getCloseAt().getMinute()) :
                TimeUtils.getUtcNow().plusDays(1)
                        .withHour(businessHour.getCloseAt().getHour())
                        .withMinute(businessHour.getCloseAt().getMinute());

        OffsetDateTime eightPM_UTC = openAt.withHour(18).withMinute(10);

        if (pickUpStart.isBefore(openAt)) {
            throw new InvalidParameterException("crud.businessHour.noOrderBeforeOpenAt");
        } else if (pickUpStart.isAfter(closeAt)) {
            throw new InvalidParameterException("crud.businessHour.noOrderAfterCloseAt");
        } else if ("production".equals(System.getenv("SPRING_PROFILES_ACTIVE")) &&
                    !lateDeliveryCustomers.contains(customerId) &&
                    pickUpStart.isAfter(eightPM_UTC)) {
            throw new InvalidParameterException("crud.businessHour.noOrderAfterCloseAt");
        }
    }

    private boolean isWithin48Hours(OffsetDateTime time, OffsetDateTime utcNow) {
        return time.getYear() == utcNow.getYear() &&
                time.isBefore(utcNow.plusHours(48));
    }

    @Override
    public ResponseEntity<List<BusinessHour>> getBusinessHours() {
        List<BusinessHour> businessHours = this.businessHourRepository.findAllByOrderByWeekDayAsc();
        return new ResponseEntity<>(businessHours, HttpStatus.OK);
    }
}
