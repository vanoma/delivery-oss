package com.vanoma.api.order.payment;

import com.vanoma.api.order.charges.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, String> {
    List<PaymentAttempt> findByRequestId(String requestId);

    List<PaymentAttempt> findByCharge(Charge charge);
}
