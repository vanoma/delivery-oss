package com.vanoma.api.order.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, String> {
    Discount findFirstByDeliveryOrderAndType(DeliveryOrder order, DiscountType type);
}
