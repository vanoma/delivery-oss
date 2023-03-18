package com.vanoma.api.order.invoices;

import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import com.vanoma.api.order.utils.annotations.PostMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMappingJson
public class InvoiceController {
    @Autowired
    private IDeliveryInvoiceService invoiceService;

    @PostMappingJson(value = "/customers/{customerId}/delivery-invoices")
    public ResponseEntity<Map<String, Object>> createInvoice(@PathVariable String customerId,
                                                             @RequestBody DeliveryInvoiceJson deliveryInvoiceJson) {
        return this.invoiceService.createAndSaveDeliveryInvoice(customerId, deliveryInvoiceJson);
    }

    @GetMapping(value = "/delivery-invoices/{deliveryInvoiceId}/status")
    public ResponseEntity<Map<String, Object>> getCustomerInvoiceStatus(@PathVariable String deliveryInvoiceId) {
        return this.invoiceService.getDeliveryInvoiceStatus(deliveryInvoiceId);
    }
}
