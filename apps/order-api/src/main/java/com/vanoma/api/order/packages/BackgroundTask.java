package com.vanoma.api.order.packages;

import com.vanoma.api.order.orders.CurrentTimeWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class BackgroundTask {

    @Autowired
    PackageRepository packageRepository;
    @Autowired
    PackageService packageService;
    @Autowired
    CurrentTimeWrapper currentTimeWrapper;

    // Runs every day at 12:00 AM to cancel requests of the previous day
    // TODO: This task keeps failing when we omit Transactional - https://sentry.io/share/issue/8992ab9db7e0419c86b7b9e1cdef15fd/
    @Scheduled(cron = "0 0 0 * * ?", zone = "UTC")
    @Transactional
    public void cancelExpiredDeliveryRequests() {
        List<Package> requestedPackages = this.packageRepository.findByStatusIn(List.of(PackageStatus.REQUEST, PackageStatus.PENDING));
        List<Package> expiredPackages = requestedPackages.stream()
                .filter(p -> Objects.isNull(p.getPickUpStart()) || p.getPickUpStart().isBefore(currentTimeWrapper.getCurrentDateTimeUtc()))
                .collect(Collectors.toList());

        for (Package pkg : expiredPackages) {
            this.packageService.cancelPackage(pkg, "Expired");
        }
    }
}
