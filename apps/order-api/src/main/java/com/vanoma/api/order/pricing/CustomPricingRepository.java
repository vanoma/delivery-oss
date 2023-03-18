package com.vanoma.api.order.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomPricingRepository extends JpaRepository<CustomPricing, String> {
    CustomPricing findFirstByCustomerIdOrderByCreatedAtDesc(String userId);

    List<CustomPricing> findByCustomerId(String customerId);
}
