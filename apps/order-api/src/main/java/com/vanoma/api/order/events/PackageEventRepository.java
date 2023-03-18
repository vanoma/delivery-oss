package com.vanoma.api.order.events;

import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.packages.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageEventRepository extends JpaRepository<PackageEvent, String>, JpaSpecificationExecutor<PackageEvent> {
    List<PackageEvent> findByPkg(Package pkg);

    List<PackageEvent> findByDeliveryOrder(DeliveryOrder deliveryOrder);

    PackageEvent findFirstByPkgAndEventName(Package pkg, EventName eventName);

    PackageEvent findFirstByPkgAndEventNameAndAssignmentId(Package pkg, EventName eventName, String assignmentId);
}
