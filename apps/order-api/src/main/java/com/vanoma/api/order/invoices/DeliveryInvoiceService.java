package com.vanoma.api.order.invoices;

import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.orders.Discount;
import com.vanoma.api.order.orders.DiscountUtils;
import com.vanoma.api.order.orders.IDeliveryOrderService;
import com.vanoma.api.order.pricing.IPricingService;
import com.vanoma.api.order.charges.ChargeUtils;
import com.vanoma.api.utils.exceptions.ResourceNotFoundException;
import com.vanoma.api.utils.web.ILanguageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DeliveryInvoiceService implements IDeliveryInvoiceService {

    @Autowired
    private DeliveryInvoiceRepository invoiceRepository;
    @Autowired
    private DeliveryInvoiceChargeRepository invoiceChargeRepository;
    @Autowired
    private IDeliveryOrderService deliveryOrderService;

    @Autowired
    private IPricingService pricingService;
    @Autowired
    private ILanguageUtils languageUtils;

    @Override
    public DeliveryInvoice save(DeliveryInvoice deliveryInvoice) {
        return this.invoiceRepository.save(deliveryInvoice);
    }

    @Override
    public DeliveryInvoice findById(String invoiceId) {
        return this.invoiceRepository.findById(invoiceId).orElse(null);
    }

    @Override
    public ResponseEntity<Map<String, Object>> createAndSaveDeliveryInvoice(String customerId, DeliveryInvoiceJson json) {
        /*
            Returns non-fully paid deliveries where DeliveryOrder.placedAt is between startAt and endAt.
         */
        List<DeliveryOrder> deliveryOrders = this.deliveryOrderService.getUnpaidDeliveryOrders(customerId, json.getStartAt(), json.getEndAt());
        DeliveryInvoice deliveryInvoice = getInvoiceForUnpaidCharges(customerId, deliveryOrders);
        return new ResponseEntity<>(Map.of("deliveryInvoiceId", deliveryInvoice.getDeliveryInvoiceId()), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getDeliveryInvoiceStatus(String invoiceId) {
        DeliveryInvoice deliveryInvoice = this.findById(invoiceId);
        if (deliveryInvoice == null) throw new ResourceNotFoundException("crud.invoice.notFound");
        Map<String, Object> response = new HashMap<>();
        response.put("status", deliveryInvoice.getStatus().name());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private DeliveryInvoice getInvoiceForUnpaidCharges(String customerId, List<DeliveryOrder> deliveryOrders) {
        Set<Charge> unpaidCharges = ChargeUtils.getUnpaidCharges(deliveryOrders);
        DeliveryInvoice deliveryInvoice = new DeliveryInvoice(customerId);
        Set<DeliveryInvoiceCharge> deliveryInvoiceCharges = createInvoiceCharges(deliveryInvoice.getDeliveryInvoiceId(), unpaidCharges);
        deliveryInvoice.setInvoiceCharges(deliveryInvoiceCharges);
        return this.save(deliveryInvoice);
    }

    private Set<DeliveryInvoiceCharge> createInvoiceCharges(String invoiceId, Set<Charge> charges) {
        Set<DeliveryInvoiceCharge> deliveryInvoiceCharges = new HashSet<>();
        charges.forEach(charge -> {
            deliveryInvoiceCharges.add(new DeliveryInvoiceCharge(invoiceId, charge));

        });
        return deliveryInvoiceCharges;
    }

}
