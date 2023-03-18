package com.vanoma.api.order.events;

import com.vanoma.api.order.packages.Package;

public interface IPackageEventService {
    PackageEvent createPackageEvent(String packageId, PackageEventJson eventJson);

    PackageEvent createPackageEvent(Package pkg, EventName eventName, String assignmentId);
}
