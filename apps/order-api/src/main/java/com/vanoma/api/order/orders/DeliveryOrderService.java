package com.vanoma.api.order.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanoma.api.order.businesshours.IBusinessHourService;
import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeRepository;
import com.vanoma.api.order.charges.ChargeType;
import com.vanoma.api.order.contacts.*;
import com.vanoma.api.order.customers.*;
import com.vanoma.api.order.events.PackageEventRepository;
import com.vanoma.api.order.external.IAuthApiCaller;
import com.vanoma.api.order.external.ICommunicationApiCaller;
import com.vanoma.api.order.external.WebPushParams;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.*;
import com.vanoma.api.order.pricing.CustomPricingRepository;
import com.vanoma.api.order.pricing.IPricingService;
import com.vanoma.api.order.utils.Dates;
import com.vanoma.api.order.utils.LanguageUtils;
import com.vanoma.api.utils.NullableValueMapBuilder;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.exceptions.ResourceNotFoundException;
import com.vanoma.api.utils.exceptions.UnauthorizedAccessException;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.input.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.vanoma.api.order.utils.AccessValidationUtils.isStaffOrService;

@Repository
public class DeliveryOrderService implements IDeliveryOrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;
    @Autowired
    private CustomPricingRepository customPricingRepository;
    @Autowired
    private ChargeRepository chargeRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private IPackageService packageService;
    @Autowired
    private IContactAddressService contactAddressService;

    @Autowired
    private LanguageUtils languageUtils;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IBusinessHourService businessHourService;
    @Autowired
    private OrderPlacementWorkflow orderPlacementWorkflow;
    @Autowired
    private IPricingService pricingService;

    @Autowired
    private ContactAddressRepository contactAddressRepository;
    @Autowired
    private IAuthApiCaller authApiCaller;

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private ICommunicationApiCaller communicationApiCaller;

    @Override
    public ResponseEntity<DeliveryOrder> createWebUserDeliveryOrder(String customerId, DeliveryOrderJson deliveryOrderJson) {
        DeliveryOrder order;

        // TODO: Make request body for creating a delivery order required once the new UI for branches is deployed.
        //  Then remove "deliveryOrderJson != null" check. Also make agentId required.
        if (deliveryOrderJson != null && deliveryOrderJson.getAgentId() != null) {
            Agent agent = this.agentRepository.getById(deliveryOrderJson.getAgentId());
            order = new DeliveryOrder(agent);
        } else {
            Customer customer = this.customerRepository.getById(customerId);
            order = new DeliveryOrder(customer);
        }

        order.setStatus(OrderStatus.STARTED).setClientType(ClientType.WEB_APP);
        return new ResponseEntity<>(this.orderRepository.save(order), HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> createApiUserDeliveryOrder(String customerId, DeliveryOrderJson deliveryOrderJson) {
        // Create a delivery order as an API user. This method executes in a transaction so that if there's an
        // exception (e.g. while creating one of  the packages), we can rollback all the changes. We don't want
        // to persist half-baked data.

        // TODO: Make request body for creating a delivery order required once the new UI for branches is deployed.
        //  Then delete this block.
        if (deliveryOrderJson == null) {
            throw new InvalidParameterException("crud.deliveryOrder.packages.required");
        }

        deliveryOrderJson.validate();
        Customer customer = this.customerRepository.getById(customerId);
        DeliveryOrder order = this.orderRepository.save(
                new DeliveryOrder(customer)
                        .setStatus(OrderStatus.STARTED)
                        .setClientType(ClientType.API));

        List<Package> packages = deliveryOrderJson.getPackages()
                .stream()
                .map(json -> this.packageService.createPackage(order, json, PackageStatus.STARTED))
                .collect(Collectors.toList());
        this.pricingService.createDeliveryFees(order);
        this.orderPlacementWorkflow.placeDeliveryOrder(order);

        Map<String, Object> payload = getCreatedOrderPayload(order, packages);
        return new ResponseEntity<>(payload, HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> createDeliveryRequest(String customerId, DeliveryRequestJson deliveryRequestJson) {
        // This method executes in a transaction so that if there's an exception (e.g. while creating one of
        // the packages), we can rollback all the changes. We don't want to persist half-baked data.

        // TODO: We should validate that delivery request contain exactly one package.
        deliveryRequestJson.validate();
        DeliveryOrder order;

        // TODO: make agentId required once the UI has been updated
        if (deliveryRequestJson.getAgentId() != null) {
            Agent agent = this.agentRepository.getById(deliveryRequestJson.getAgentId());
            order = new DeliveryOrder(agent);
        } else {
            Customer customer = this.customerRepository.getById(customerId);
            order = new DeliveryOrder(customer);
        }

        DeliveryOrder savedOrder = this.orderRepository.save(order
                .setStatus(OrderStatus.REQUEST)
                .setClientType(ClientType.DELIVERY_LINK)
                .setIsCustomerPaying(deliveryRequestJson.getIsCustomerPaying()));

        List<Package> packages = deliveryRequestJson.getPackages()
                .stream().
                map(packageJson -> this.packageService.createPackage(savedOrder, packageJson, PackageStatus.REQUEST))
                .collect(Collectors.toList());
        this.sendDeliveryRequestMessages(savedOrder, packages);

        Map<String, Object> response = Map.of(
                "deliveryOrderId", savedOrder.getDeliveryOrderId(),
                "deliveryLink", savedOrder.getDeliveryLink()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Map<String, Object>> createLinkOpeningActions(String deliveryOrderId) {
        DeliveryOrder deliveryOrder = this.orderRepository.getById(deliveryOrderId);
        deliveryOrder.setLinkOpenedAt();
        this.orderRepository.save(deliveryOrder);
        return new ResponseEntity<>(Map.of("deliveryOrderId", deliveryOrderId), HttpStatus.OK);
    }

    @Override
    public void placeDeliveryOrder(String deliveryOrderId, String authHeader) {
        DeliveryOrder order = this.orderRepository.getById(deliveryOrderId);

        if (!isStaffOrService(authHeader) && order.getCustomer().getIsPrepaid()) {
            throw new UnauthorizedAccessException("crud.deliveryOrder.placement.notAuthorized");
        }

        this.orderPlacementWorkflow.placeDeliveryOrder(order);
    }

    @Override
    public ResponseEntity<Map<String, Object>> prePlaceDeliveryOrder(String deliveryOrderId) {
        DeliveryOrder order = this.orderRepository.findById(deliveryOrderId).orElse(null);
        if (order == null) throw new ResourceNotFoundException("crud.deliveryOrder.notFound");

        List<Package> packages = this.packageRepository.findByDeliveryOrder(order);
        List<Charge> charges = this.chargeRepository.findByDeliveryOrderAndType(order, ChargeType.DELIVERY_FEE);
        PackageUtils.validateDeliveryOrderPackages(packages, charges);

        order.setStatus(OrderStatus.PENDING);
        this.orderRepository.save(order);

        packages.forEach(pkg -> pkg.setStatus(PackageStatus.PENDING));
        this.packageRepository.saveAll(packages);
        this.sendCustomerSMSforPendingOrder(order);

        Map<String, Object> response = Map.of("deliveryOrderId", deliveryOrderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DeliveryOrder> duplicateDeliveryOrder(String deliveryOrderId,
                                                                OrderDuplicationJson orderDuplicationJson) {
        DeliveryOrder oldOrder = this.orderRepository.getById(deliveryOrderId);
        OrderStatus status = oldOrder.getStatus();
        if (status != OrderStatus.PLACED && status != OrderStatus.COMPLETE) {
            throw new InvalidParameterException("crud.deliveryOrder.duplication.invalidStatus");
        }
        DeliveryOrder duplicateOrder = this.getDuplicateOrder(oldOrder, orderDuplicationJson);
        this.orderPlacementWorkflow.placeDeliveryOrder(duplicateOrder);
        return new ResponseEntity<>(duplicateOrder, HttpStatus.CREATED);
    }

    @Override
    public List<DeliveryOrder> getUnpaidDeliveryOrders(String customerId, String startAt, String endAt) {
        if (startAt != null && endAt != null) {
            OffsetDateTime placedAfter = TimeUtils.parseISOString(startAt);
            OffsetDateTime placedBefore = TimeUtils.parseISOString(endAt);
            return this.orderRepository.getUnpaidDeliveryOrders(customerId, placedAfter, placedBefore);
        } else if (startAt != null) {
            OffsetDateTime placedAfter = TimeUtils.parseISOString(startAt);
            return this.orderRepository.getUnpaidDeliveryOrdersPlacedAfter(customerId, placedAfter);
        } else if (endAt != null) {
            OffsetDateTime placedBefore = TimeUtils.parseISOString(endAt);
            return this.orderRepository.getUnpaidDeliveryOrdersPlacedBefore(customerId, placedBefore);
        } else {
            return this.orderRepository.getUnpaidDeliveryOrders(customerId);
        }
    }

    private void sendDeliveryRequestMessages(DeliveryOrder deliveryOrder, List<Package> packages) {
        Package pkg = packages.get(0); // Delivery requests can only have one package
        Customer customer = deliveryOrder.getCustomer();
        String contactPhoneNumber = pkg.getToContact().getPhoneNumberOne();

        // Send SMS to customer
        String smsChannelQueryParam = "&c=sms";
        String smsText = customer.getBusinessName() + " (" + customer.getPhoneNumber().substring(2) +
                ") sent a delivery request. " +
                "Share your location here: " + deliveryOrder.getDeliveryLink() + smsChannelQueryParam +
                ". Call 8080 (Toll-Free) for questions.";
        CompletableFuture<HttpResult> futureResult = this.communicationApiCaller.sendSMS(smsText, contactPhoneNumber);
        HttpResult smsResult = futureResult.join();
        if (!smsResult.isSuccess()) {
            throw new InvalidParameterException("apis.communication.smsNotSent");
        }

        // Send web push to operator
        WebPushParams params = WebPushParams.builder()
                .heading("New Delivery Request!")
                .message("Delivery request from " + customer.getBusinessName() + " to " + contactPhoneNumber)
                .receiverIds(Arrays.asList(System.getenv("WEB_PUSH_OPERATOR_ID")))
                .jsonData(Map.of(
                        "deliveryOrderId", deliveryOrder.getDeliveryOrderId(),
                        "packageId", pkg.getPackageId(),
                        // Because we are running in a transaction which gets flushed after this running this code,
                        // createdAt is not available for both order and package. So we are just using the current time.
                        "createdAt", Dates.stringifyDatetime(OffsetDateTime.now().withNano(0))
                        )
                )
                .metadata(new NullableValueMapBuilder<String, String>()
                        .put("appId", System.getenv("WEB_PUSH_OPERATOR_APP_ID"))
                        .put("apiKey", System.getenv("WEB_PUSH_OPERATOR_API_KEY"))
                        .build()
                )
                .build();
        this.communicationApiCaller.sendWebPush(params);
    }

    @Async
    private void sendCustomerSMSforPendingOrder(DeliveryOrder order) {
        Customer customer = order.getCustomer();
        String dashboardAppUrl = System.getenv("VANOMA_DASHBOARD_APP_URL");
        String message = "A customer just provided their address. Please pay for the delivery here: " +
                dashboardAppUrl + "/deliveries/request. Call 8080 (Toll-Free) for any questions.";
        this.communicationApiCaller.sendSMS(message, customer.getPhoneNumber());
    }

    private Map<String, Object> getCreatedOrderPayload(DeliveryOrder order, List<Package> packages) {
        // Create appropriate response body for API users. This method is called prior to finalizing the database
        // transaction. As result, updatedAt and createdAt can still be null or have a non-final value (i.e. updatedAt).
        // To avoid tests from failing, they are being omitted from the serialized order and package objects.
        List<Map<String, Object>> packageMapList = packages.stream()
                .map(pkg -> new NullableValueMapBuilder<String, Object>()
                        .put("packageId", pkg.getPackageId())
                        .put("priority", pkg.getPriority().name())
                        .put("trackingNumber", pkg.getTrackingNumber())
                        .put("trackingLink", pkg.getTrackingLink())
                        .put("size", pkg.getSize().name())
                        .put("status", pkg.getStatus().name())
                        .put("fromContact", pkg.getFromContact())
                        .put("toContact", pkg.getToContact())
                        .put("fromAddress", pkg.getFromAddress())
                        .put("toAddress", pkg.getToAddress())
                        .put("fromNote", pkg.getFromNote())
                        .put("toNote", pkg.getToNote())
                        .put("pickUpStart", pkg.getPickUpStart())
                        .put("pickUpEnd", pkg.getPickUpEnd())
                        .put("fragileContent", pkg.getFragileContent())
                        .put("eventCallback", pkg.getEventCallback())
                        .build()).collect(Collectors.toList());

        return new NullableValueMapBuilder<String, Object>()
                .put("deliveryOrderId", order.getDeliveryOrderId())
                .put("customerId", order.getCustomerId())
                .put("placedAt", order.getPlacedAt())
                .put("isCustomerPaying", order.getIsCustomerPaying())
                .put("clientType", order.getClientType().name())
                .put("status", order.getStatus().name())
                .put("deliveryLink", order.getDeliveryLink())
                .put("linkOpenedAt", order.getLinkOpenedAt())
                .put("packages", packageMapList)
                .build();
    }

    private DeliveryOrder getDuplicateOrder(DeliveryOrder deliveryOrder,
                                            OrderDuplicationJson orderDuplicationJson) {
        DeliveryOrder newOrder = deliveryOrder.getAgent() != null
                ? new DeliveryOrder(deliveryOrder.getAgent())
                : new DeliveryOrder(deliveryOrder.getCustomer());

        this.orderRepository.save(newOrder.setClientType(ClientType.STAFF));
        deliveryOrder.getPackages()
                .stream()
                .filter(p -> p.getStatus() != PackageStatus.CANCELED)
                .forEach(p -> this.packageService.duplicatePackage(
                        p, newOrder, orderDuplicationJson.getPickUpStart())
                );
        return this.orderRepository.save(newOrder);
    }
}
