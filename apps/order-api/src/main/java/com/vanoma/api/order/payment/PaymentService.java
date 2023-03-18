package com.vanoma.api.order.payment;

import com.vanoma.api.order.businesshours.IBusinessHourService;
import com.vanoma.api.order.charges.*;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.customers.CustomerRepository;
import com.vanoma.api.order.external.PaymentRecordParams;
import com.vanoma.api.order.orders.*;
import com.vanoma.api.order.external.IPaymentAPICaller;
import com.vanoma.api.order.external.PaymentRequestParams;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.order.packages.PackageUtils;
import com.vanoma.api.order.utils.LanguageUtils;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.input.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PaymentService implements IPaymentService {

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderPlacementWorkflow orderPlacementWorkflow;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private LanguageUtils languageUtils;
    @Autowired
    private IBusinessHourService businessHourService;
    @Autowired
    private IPaymentAPICaller paymentAPICaller;
    @Lazy
    @Autowired
    private IDeliveryOrderService deliveryOrderService;

    @Override
    public ResponseEntity<Map<String, Object>> requestPaymentOneOrder(String deliveryOrderId,
                                                                      PaymentRequestOneOrderJson json) {
        json.validate();
        DeliveryOrder deliveryOrder = this.orderRepository.getById(deliveryOrderId);
        List<Package> packages = this.packageRepository.findByDeliveryOrder(deliveryOrder);
        List<Charge> charges = this.chargeRepository.findByDeliveryOrderAndType(deliveryOrder, ChargeType.DELIVERY_FEE);
        PackageUtils.validateDeliveryOrderPackages(packages, charges);

        this.businessHourService.validateBusinessHours(new ArrayList<>(deliveryOrder.getPackages()), deliveryOrder.getCustomerId());
        if (deliveryOrder.getPaymentStatus() == PaymentStatus.PAID) {
            return getPaymentAlreadyDoneResponse();
        }

        Set<Charge> unpaidCharges = deliveryOrder.getUnpaidCharges();
        Set<Discount> pendingDiscounts = deliveryOrder.getPendingDiscounts();
        if (unpaidCharges.size() == 0) return getNoUnpaidChargesResponse();

        return requestPayment(unpaidCharges, pendingDiscounts, json.getPaymentMethodId(), null);
    }

    @Override
    public ResponseEntity<Map<String, Object>> requestPaymentManyOrders(String customerId,
                                                                        PaymentRequestManyOrdersJson json) {
        json.validate();
        List<DeliveryOrder> unpaidOrders = this.deliveryOrderService.getUnpaidDeliveryOrders(customerId, null, json.getEndAt());

        if (Objects.nonNull(json.getBranchId())) {
            unpaidOrders = unpaidOrders.stream()
                    .filter(order -> Objects.nonNull(order.getBranch()) && order.getBranch().getBranchId().equalsIgnoreCase(json.getBranchId()))
                    .collect(Collectors.toList());
        }

        Set<Charge> unpaidCharges = ChargeUtils.getUnpaidCharges(unpaidOrders);
        Set<Discount> pendingDiscounts = DiscountUtils.getPendingDiscount(unpaidOrders);
        if (unpaidCharges.size() == 0) return getNoUnpaidChargesResponse();

        return requestPayment(unpaidCharges, pendingDiscounts, json.getPaymentMethodId(), json.getTotalAmount());
    }

    @Override
    public ResponseEntity<Map<String, Object>> processPaymentCallback(String paymentRequestId, PaymentCallbackJson json) {
        json.validate();
        PaymentRequest paymentRequest = this.paymentRequestRepository.getById(paymentRequestId);
        this.paymentRequestRepository.save(paymentRequest.setIsSuccess(json.isSuccess()));

        if (json.isSuccess()) {
            List<Charge> charges = paymentRequest.getCharges().stream()
                    .map(c -> c.setStatus(ChargeStatus.PAID))
                    .collect(Collectors.toList());
            this.chargeRepository.saveAll(charges);

            List<Discount> discounts = paymentRequest.getDiscounts().stream()
                    .map(d -> d.setStatus(DiscountStatus.APPLIED))
                    .collect(Collectors.toList());
            this.discountRepository.saveAll(discounts);

            this.orderPlacementWorkflow.placeDeliveryOrder(charges.get(0).getDeliveryOrder());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Callback processed successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> confirmOfflinePaymentOneOrder(String deliveryOrderId,
                                                                             OfflinePaymentOneOrderJson json) {
        json.validate();
        DeliveryOrder order = this.orderRepository.getById(deliveryOrderId);
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.COMPLETE) {
            throw new InvalidParameterException("crud.paymentAttempt.invalidOrderStatus");
        }

        Set<Charge> unpaidCharges = order.getUnpaidCharges();
        Set<Discount> pendingDiscounts = order.getPendingDiscounts();
        if (unpaidCharges.size() == 0) return getNoUnpaidChargesResponse();

        HttpResult result = confirmOfflinePayment(json, unpaidCharges, pendingDiscounts);
        if (!result.isSuccess()) {
            return result.getResponseEntityMessageFromHttpResult(languageUtils);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("deliveryOrderId", deliveryOrderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> confirmOfflinePaymentManyOrders(String customerId,
                                                                               OfflinePaymentManyOrdersJson json) {
        json.validate();
        List<DeliveryOrder> unpaidOrders = this.deliveryOrderService.getUnpaidDeliveryOrders(customerId, null, json.getEndAt());
        Set<Charge> unpaidCharges = ChargeUtils.getUnpaidCharges(unpaidOrders);
        Set<Discount> pendingDiscounts = DiscountUtils.getPendingDiscount(unpaidOrders);
        if (unpaidCharges.size() == 0) return getNoUnpaidChargesResponse();

        HttpResult result = confirmOfflinePayment(json, unpaidCharges, pendingDiscounts);
        if (!result.isSuccess()) {
            return result.getResponseEntityMessageFromHttpResult(languageUtils);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("customerId", customerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getPaymentRequestStatus(String paymentRequestId) {
        PaymentRequest paymentRequest = this.paymentRequestRepository.getById(paymentRequestId);
        PaymentStatus status = ChargeUtils.getPaymentStatus(paymentRequest.getCharges());
        Map<String, Object> response = new HashMap<>();
        response.put("paymentStatus", status.name());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public Map<String, Object> getCustomerSpending(String customerId, String endAt, String branchId) {
        List<DeliveryOrder> deliveryOrders = this.deliveryOrderService.getUnpaidDeliveryOrders(customerId, null, endAt);

        if (Objects.nonNull(branchId)) {
            deliveryOrders = deliveryOrders.stream()
                    .filter(order -> Objects.nonNull(order.getBranch()) && order.getBranch().getBranchId().equalsIgnoreCase(branchId))
                    .collect(Collectors.toList());
        }

        if (deliveryOrders.size() == 0) {
            return Map.of(
                    "message", languageUtils.getLocalizedMessage("crud.spending.noUnpaidCharges"),
                    "deliveryOrders", new ArrayList<>(),
                    "totalCount", 0,
                    "totalAmount", BigDecimal.ZERO,
                    "transactionAmount", BigDecimal.ZERO,
                    "transactionFee", BigDecimal.ZERO
            );
        }

        Set<Charge> unpaidCharges = ChargeUtils.getUnpaidCharges(deliveryOrders);
        Set<Discount> pendingDiscounts = DiscountUtils.getPendingDiscount(deliveryOrders);
        BigDecimal transactionAmount = ChargeUtils.getTransactionAmount(unpaidCharges, pendingDiscounts);
        BigDecimal transactionFee = ChargeUtils.computeTransactionFeeGivenTransactionAmount(transactionAmount);

        return Map.of(
                "deliveryOrders", deliveryOrders,
                "totalCount", deliveryOrders.size(),
                "totalAmount", transactionAmount.add(transactionFee),
                "transactionAmount", transactionAmount,
                "transactionFee", transactionFee
        );
    }

    @Override
    public Map<String, Object> getBillingStatus(String customerId) {
        Customer customer = this.customerRepository.getById(customerId);
        DeliveryOrder earliestOrder = this.deliveryOrderService.getUnpaidDeliveryOrders(customerId, null, null)
                .stream()
                .min(Comparator.comparing(DeliveryOrder::getPlacedAt))
                .orElse(null);

        if (earliestOrder == null) {
            return Map.of("isBillDue", false, "gracePeriod", 0);
        }

        // Customer **must** pay after their "billingInterval" days, from the earliest delivery's placedAt.
        // We add a "billingGracePeriod" days a grace period for completely making them unable to place new orders
        OffsetDateTime billDueOn = earliestOrder.getPlacedAt().plusDays(customer.getBillingInterval());
        OffsetDateTime lockAccountOn = billDueOn.plusDays(customer.getBillingGracePeriod());
        Duration durationUntilDueDate = Duration.between(TimeUtils.getUtcNow(), billDueOn);
        Duration durationUntilLockout = Duration.between(TimeUtils.getUtcNow(), lockAccountOn);
        boolean isBillDue = durationUntilDueDate.toDays() <= 0;
        long gracePeriod = durationUntilLockout.toDays() > 0 ? durationUntilLockout.toDays() : 0;
        return Map.of("isBillDue", isBillDue, "gracePeriod", gracePeriod);
    }

    private ResponseEntity<Map<String, Object>> getNoUnpaidChargesResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", languageUtils.getLocalizedMessage("crud.spending.noUnpaidCharges"));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Map<String, Object>> requestPayment(Set<Charge> unpaidCharges,
                                                               Set<Discount> pendingDiscounts,
                                                               String paymentMethodId,
                                                               BigDecimal expectedTotalAmount) {
        PaymentRequest paymentRequest = new PaymentRequest(unpaidCharges, pendingDiscounts);
        this.paymentRequestRepository.save(paymentRequest);

        PaymentRequestParams paymentRequestParams = new PaymentRequestParams()
                .setPaymentMethodId(paymentMethodId)
                .setTransactionBreakdown(ChargeUtils.getTransactionBreakdown(unpaidCharges, pendingDiscounts, expectedTotalAmount))
                .setPaymentRequestId(paymentRequest.getPaymentRequestId());

        HttpResult result = this.paymentAPICaller.requestPayment(paymentRequestParams);
        if (result.isSuccess()) {
            result.getBody().put("paymentRequestId", paymentRequest.getPaymentRequestId());
        }

        return new ResponseEntity<>(result.getBody(), result.getHttpStatus());
    }

    private ResponseEntity<Map<String, Object>> getPaymentAlreadyDoneResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("message",
                languageUtils.getLocalizedMessage("crud.paymentAttempt.alreadyComplete"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private HttpResult confirmOfflinePayment(OfflinePaymentOneOrderJson json,
                                             Set<Charge> unpaidCharges,
                                             Set<Discount> pendingDiscounts) {
        PaymentRequest paymentRequest = new PaymentRequest(unpaidCharges, pendingDiscounts).setIsSuccess(true);
        this.paymentRequestRepository.save(paymentRequest);

        TransactionBreakdown breakdown = ChargeUtils.getTransactionBreakdown(unpaidCharges, pendingDiscounts, json.getTotalAmount());
        PaymentRecordParams paymentRecord = createPaymentRecordParams(json, paymentRequest.getPaymentRequestId(), breakdown);
        HttpResult result = this.paymentAPICaller.confirmPayment(paymentRecord);
        if (result.isSuccess()) {
            List<Charge> charges = unpaidCharges.stream()
                    .map(c -> c.setStatus(ChargeStatus.PAID))
                    .collect(Collectors.toList());
            this.chargeRepository.saveAll(charges);

            List<Discount> discounts = pendingDiscounts.stream()
                    .map(d -> d.setStatus(DiscountStatus.APPLIED))
                    .collect(Collectors.toList());
            this.discountRepository.saveAll(discounts);
        }

        return result;
    }

    private PaymentRecordParams createPaymentRecordParams(OfflinePaymentOneOrderJson json,
                                                          String paymentRequestId,
                                                          TransactionBreakdown breakdown) {
        PaymentRecordParams paymentRecord = new PaymentRecordParams()
                .setOperatorTransactionId(json.getOperatorTransactionId())
                .setPaymentTime(json.getPaymentTime().toString());
        paymentRecord.setPaymentMethodId(json.getPaymentMethodId());
        paymentRecord.setPaymentRequestId(paymentRequestId);
        paymentRecord.setDescription(json.getDescription());
        paymentRecord.setTransactionBreakdown(breakdown);
        return paymentRecord;
    }
}
