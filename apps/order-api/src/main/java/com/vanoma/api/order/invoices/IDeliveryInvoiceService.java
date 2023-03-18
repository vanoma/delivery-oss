package com.vanoma.api.order.invoices;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface IDeliveryInvoiceService {

    DeliveryInvoice save(DeliveryInvoice deliveryInvoice);

    DeliveryInvoice findById(String invoiceIdF);

    ResponseEntity<Map<String, Object>> createAndSaveDeliveryInvoice(String customerId, DeliveryInvoiceJson json);

    ResponseEntity<Map<String, Object>> getDeliveryInvoiceStatus(String invoiceId);
}
