package com.vanoma.api.order.packages;

import com.vanoma.api.order.charges.ChargeRepository;
import com.vanoma.api.order.events.PackageEventRepository;
import com.vanoma.api.order.orders.CurrentTimeWrapper;
import com.vanoma.api.order.tests.OrderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
public class BackgroundTaskTest {
    @Autowired
    PackageRepository packageRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;
    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    OrderFactory orderFactory;

    @Autowired
    BackgroundTask backgroundTask;
    @MockBean
    CurrentTimeWrapper currentTimeWrapper;

    @BeforeEach
    public void setUp() {
        // Delete existing packages to avoid polluting the next test. We have to delete events and
        // charges first because cascading deletes is not working :(
        this.packageEventRepository.deleteAll();
        this.chargeRepository.deleteAll();
        this.packageRepository.deleteAll();
    }

    @Test
    public void testCancelExpiredDeliveryRequests_excludesNonPendingAndRequestStatuses() {
        List<Package> packages = List.of(
                this.orderFactory.createPackage(PackageStatus.REQUEST),
                this.orderFactory.createPackage(PackageStatus.PENDING),
                this.orderFactory.createPackage(PackageStatus.STARTED),
                this.orderFactory.createPackage(PackageStatus.PLACED)
        );

        backgroundTask.cancelExpiredDeliveryRequests();

        assertThat(this.packageRepository.getById(packages.get(0).getPackageId()).getStatus()).isEqualTo(PackageStatus.INCOMPLETE);
        assertThat(this.packageRepository.getById(packages.get(1).getPackageId()).getStatus()).isEqualTo(PackageStatus.INCOMPLETE);
        assertThat(this.packageRepository.getById(packages.get(2).getPackageId()).getStatus()).isEqualTo(PackageStatus.STARTED);
        assertThat(this.packageRepository.getById(packages.get(3).getPackageId()).getStatus()).isEqualTo(PackageStatus.PLACED);
    }

    @Test
    public void testCancelExpiredDeliveryRequests_cancelsRequestsWithoutPickUpStart() {
        List<Package> packages = List.of(
                this.orderFactory.createPackage(PackageStatus.REQUEST),
                this.orderFactory.createPackage(PackageStatus.STARTED)
        );

        backgroundTask.cancelExpiredDeliveryRequests();

        assertThat(this.packageRepository.getById(packages.get(0).getPackageId()).getStatus()).isEqualTo(PackageStatus.INCOMPLETE);
        assertThat(this.packageRepository.getById(packages.get(1).getPackageId()).getStatus()).isEqualTo(PackageStatus.STARTED);
    }

    @Test
    public void testCancelExpiredDeliveryRequests_cancelsRequestsWithPastPickUpStart() {
        OffsetDateTime utcNow = OffsetDateTime.now();
        List<Package> packages = List.of(
                this.orderFactory.createPackage(PackageStatus.REQUEST, utcNow.minusHours(1)),
                this.orderFactory.createPackage(PackageStatus.REQUEST, utcNow.plusHours(1))
        );

        when(this.currentTimeWrapper.getCurrentDateTimeUtc()).thenReturn(utcNow);
        backgroundTask.cancelExpiredDeliveryRequests();

        assertThat(this.packageRepository.getById(packages.get(0).getPackageId()).getStatus()).isEqualTo(PackageStatus.INCOMPLETE);
        assertThat(this.packageRepository.getById(packages.get(1).getPackageId()).getStatus()).isEqualTo(PackageStatus.REQUEST);
    }
}
