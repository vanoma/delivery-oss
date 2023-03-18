package com.vanoma.api.order.invoices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryInvoiceChargeRepository extends JpaRepository<DeliveryInvoiceCharge, String> {
}
