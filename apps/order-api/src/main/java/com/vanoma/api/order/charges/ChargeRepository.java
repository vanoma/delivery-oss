package com.vanoma.api.order.charges;

import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.packages.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, String>, JpaSpecificationExecutor<Charge> {

    List<Charge> findByDeliveryOrder(DeliveryOrder deliveryOrder);

    List<Charge> findByDeliveryOrderAndType(DeliveryOrder deliveryOrder, ChargeType type);

    Charge findFirstByPkgAndType(Package pkg, ChargeType type);

    List<Charge> findByPkg(Package pkg);

    @Transactional
    void deleteByPkg(Package pkg);
}
