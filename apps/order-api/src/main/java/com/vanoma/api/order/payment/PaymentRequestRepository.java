package com.vanoma.api.order.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, String> {
    List<PaymentRequest> findByChargesChargeId(String chargeId);
}
