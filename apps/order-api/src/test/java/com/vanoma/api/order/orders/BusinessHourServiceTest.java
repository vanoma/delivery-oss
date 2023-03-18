package com.vanoma.api.order.orders;

import com.vanoma.api.order.businesshours.BusinessHour;
import com.vanoma.api.order.businesshours.BusinessHourJson;
import com.vanoma.api.order.businesshours.BusinessHourRepository;
import com.vanoma.api.order.businesshours.IBusinessHourService;
import com.vanoma.api.order.tests.OrderFactory;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.input.TimeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BusinessHourServiceTest {

    @Autowired
    private IBusinessHourService businessHourService;
    @Autowired
    private BusinessHourRepository businessHourRepository;
    @MockBean
    private CurrentTimeWrapper currentTimeWrapperMock;
    @Autowired
    private OrderFactory orderFactory;

    private OffsetTime buildOpenAt() {
        return OffsetTime.parse("07:00:00Z");
    }

    private OffsetTime buildCloseAt() {
        return OffsetTime.parse("22:00:00Z");
    }

    private void createBusinessHours(OffsetTime openAt, OffsetTime closeAt) {
        for (int i = 1; i <= 7; i++ ) {
            BusinessHourJson json = new BusinessHourJson()
                    .setWeekDay(i)
                    .setOpenAt(openAt.toString())
                    .setCloseAt(closeAt.toString());

            ResponseEntity<BusinessHour> entity = this.businessHourService.createOrUpdate(json);
            assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    public void testCreateOrUpdate_returnsErrorWhenWeekDayIsMissing() {
        BusinessHourJson json = new BusinessHourJson()
                .setOpenAt(buildOpenAt().toString())
                .setCloseAt(buildCloseAt().toString())
                .setIsDayOff(false);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.weekDay.required");
    }

    @Test
    public void testCreateOrUpdate_returnsErrorWhenWeekDayIsBelowOne() {
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(0)
                .setOpenAt(buildOpenAt().toString())
                .setCloseAt(buildCloseAt().toString())
                .setIsDayOff(false);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.weekDay.invalid");
    }

    @Test
    public void testCreateOrUpdate_returnsErrorWhenWeekDayIsAboveOne() {
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(8)
                .setOpenAt(buildOpenAt().toString())
                .setCloseAt(buildCloseAt().toString())
                .setIsDayOff(false);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.weekDay.invalid");
    }

    @Test
    public void testCreateOrUpdate_returnsErrorWhenIsNotDayOffAndMissingOpenAt() {
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(1)
                .setCloseAt(OffsetTime.now(Clock.systemUTC()).toString())
                .setIsDayOff(false);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.openAt.required");
    }

    @Test
    public void testCreateOrUpdate_returnsErrorWhenIsNotDayOffAndMissingCloseAt() {
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(1)
                .setOpenAt(OffsetTime.now(Clock.systemUTC()).toString())
                .setIsDayOff(false);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.closeAt.required");
    }

    @Test
    public void testCreateOrUpdate_createsBusinessWithIsDayOffSetToFalseByDefault() {
        OffsetTime openAt = buildOpenAt();
        OffsetTime closeAt = buildCloseAt();
        createBusinessHours(openAt, closeAt);

        BusinessHour businessHour = this.businessHourRepository.findById(1).orElse(null);
        assertThat(businessHour).isNotNull();
        assertThat(businessHour.getIsDayOff()).isEqualTo(false);
        assertThat(businessHour.getOpenAt().isEqual(openAt)).isTrue();
        assertThat(businessHour.getCloseAt().isEqual(closeAt)).isTrue();
    }

    @Test
    public void testCreateOrUpdate_returnsErrorWhenOpenAtIsSpecifiedForDayOff() {
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(1)
                .setOpenAt(OffsetTime.now(Clock.systemUTC()).toString())
                .setIsDayOff(true);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.noOpenAtForDayOff");
    }

    @Test
    public void testCreateOrUpdate_returnsErrorWhenCloseAtIsSpecifiedForDayOff() {
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(1)
                .setCloseAt(OffsetTime.now(Clock.systemUTC()).toString())
                .setIsDayOff(true);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.noCloseAtForDayOff");
    }

    @Test
    public void testCreateOrUpdate_createsDayOffWithDayOffSetToTrue() {
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(2)
                .setIsDayOff(true);

        ResponseEntity<BusinessHour> entity = this.businessHourService.createOrUpdate(json);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        BusinessHour businessHour = this.businessHourRepository.findById(2).orElse(null);
        assertThat(businessHour).isNotNull();
        assertThat(businessHour.getIsDayOff()).isEqualTo(true);
        assertThat(businessHour.getOpenAt()).isNull();
        assertThat(businessHour.getCloseAt()).isNull();
    }

    @Test
    public void testCreateOrUpdate_returnsErrorWhenOpenAtAndCloseAtAreLessThanThreeHoursApart() {
        OffsetTime openAt = buildOpenAt();
        OffsetTime closeAt = openAt.plusMinutes(30);
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(1)
                .setOpenAt(openAt.toString())
                .setCloseAt(closeAt.toString());

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.insufficientInterval");
    }


    @Test
    public void testCreateOrUpdate_returnsErrorWhenOpenAtIsAfterCloseAt() {
        OffsetTime openAt = buildOpenAt();
        OffsetTime closeAt = openAt.minusHours(4); // Minus hours
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(1)
                .setOpenAt(openAt.toString())
                .setCloseAt(closeAt.toString());

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.createOrUpdate(json);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.openAtAfterCloseAt");
    }

    @Test
    public void testCreateOrUpdate_updatesExistingWeekDay() {
        OffsetTime openAt = buildOpenAt();
        OffsetTime closeAt = buildCloseAt();
        createBusinessHours(openAt, closeAt);
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(1)
                .setOpenAt(buildOpenAt().toString())
                .setCloseAt(buildCloseAt().toString())
                .setIsDayOff(false);

        BusinessHour businessHour = this.businessHourRepository.findById(1).orElse(null);
        assertThat(businessHour).isNotNull();
        assertThat(businessHour.getIsDayOff()).isEqualTo(false);
        assertThat(businessHour.getOpenAt().isEqual(openAt)).isTrue();
        assertThat(businessHour.getCloseAt().isEqual(closeAt)).isTrue();

        OffsetTime newCloseAt = closeAt.plusMinutes(30);
        json.setCloseAt(newCloseAt.toString());

        ResponseEntity<BusinessHour> entityTwo = this.businessHourService.createOrUpdate(json);
        assertThat(entityTwo.getStatusCode()).isEqualTo(HttpStatus.OK);

        BusinessHour newlySaved = this.businessHourRepository.findById(1).orElse(null);
        assertThat(newlySaved).isNotNull();
        assertThat(newlySaved.getCloseAt().isEqual(newCloseAt)).isTrue();
    }


    @Test
    public void testVerifyBusinessHours_returnsErrorWhenPickUpIsAfterBusinessHours() {
        OffsetTime openAt = buildOpenAt();
        OffsetTime closeAt = buildCloseAt();

        createBusinessHours(openAt, closeAt);

        OffsetDateTime utcNow = TimeUtils.getUtcNow();
        OffsetDateTime pickUpStart = utcNow.withHour(closeAt.getHour())
                .withMinute(closeAt.getMinute() + 1); // Pick up after close time
        Package pkg = this.orderFactory.createPackage()
                .setPickUpStart(pickUpStart);

        when(this.currentTimeWrapperMock.getCurrentTimeUtc())
                .thenReturn(openAt.plusMinutes(15));

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.validateBusinessHours(List.of(pkg), null);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.noOrderAfterCloseAt");
    }

    @Test
    public void testVerifyBusinessHours_returnsErrorWhenPickUpIsBeforeBusinessHours() {
        int today = TimeUtils.getUtcNow().getDayOfWeek().getValue(); // between 1 & 7;

        OffsetTime openAt = buildOpenAt();
        OffsetTime closeAt = buildCloseAt();

        createBusinessHours(openAt, closeAt);

        when(this.currentTimeWrapperMock.getCurrentTimeUtc())
                .thenReturn(openAt.minusHours(2));

        OffsetDateTime pickUpStart = TimeUtils.getUtcNow()
                .withHour(openAt.getHour() - 1);
        Package pkg = this.orderFactory.createPackage()
                .setPickUpStart(pickUpStart);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.validateBusinessHours(List.of(pkg), null);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.noOrderBeforeOpenAt");
    }

    @Test
    public void testVerifyBusinessHours_returnsErrorWhenCalledOnHolidays() {
        int today = TimeUtils.getUtcNow().getDayOfWeek().getValue(); // between 1 & 7;
        OffsetTime openAt = buildOpenAt();
        BusinessHourJson json = new BusinessHourJson()
                .setWeekDay(today)
                .setIsDayOff(true);

        ResponseEntity<BusinessHour> entity = this.businessHourService.createOrUpdate(json);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        int tomorrow = today == 7 ? 1 : today + 1;
        BusinessHourJson tomorrowJson = new BusinessHourJson()
                .setWeekDay(tomorrow)
                .setIsDayOff(true);

        ResponseEntity<BusinessHour> entityTwo = this.businessHourService.createOrUpdate(tomorrowJson);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        when(this.currentTimeWrapperMock.getCurrentTimeUtc())
                .thenReturn(openAt);

        OffsetDateTime pickUpStart = TimeUtils.getUtcNow()
                .withHour(openAt.getHour() - 1);
        Package pkg = this.orderFactory.createPackage()
                .setPickUpStart(pickUpStart);

        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.businessHourService.validateBusinessHours(List.of(pkg), null);
        });
        assertThat(exception.getMessage()).isEqualTo("crud.businessHour.noOrderOnDayOff");
    }

    @Test
    public void testGetBusinessHours_returnsAllBusinessHoursSortedByDayInAscOrder() {
        BusinessHourJson monday = new BusinessHourJson()
                .setWeekDay(1)
                .setIsDayOff(true);
        this.businessHourService.createOrUpdate(monday);
        BusinessHourJson tuesday = new BusinessHourJson()
                .setWeekDay(2)
                .setIsDayOff(true);
        this.businessHourService.createOrUpdate(tuesday);
        BusinessHourJson wednesday = new BusinessHourJson()
                .setWeekDay(3)
                .setIsDayOff(true);
        this.businessHourService.createOrUpdate(wednesday);

        ResponseEntity<List<BusinessHour>> entity = this.businessHourService.getBusinessHours();
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BusinessHour> businessHours = entity.getBody();
        assertThat(businessHours).isNotNull();

        assertThat(businessHours.get(0).getWeekDay()).isEqualTo(monday.getWeekDay());
        assertThat(businessHours.get(1).getWeekDay()).isEqualTo(tuesday.getWeekDay());
        assertThat(businessHours.get(2).getWeekDay()).isEqualTo(wednesday.getWeekDay());
    }
}
