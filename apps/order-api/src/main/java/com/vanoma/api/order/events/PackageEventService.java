package com.vanoma.api.order.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.customers.CustomerRepository;
import com.vanoma.api.order.external.IAuthApiCaller;
import com.vanoma.api.order.external.ICommunicationApiCaller;
import com.vanoma.api.order.external.CallbackParams;
import com.vanoma.api.order.external.WebPushParams;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.orders.OrderRepository;
import com.vanoma.api.order.orders.OrderStatus;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageContacts;
import com.vanoma.api.order.packages.PackageRepository;
import com.vanoma.api.order.packages.PackageStatus;
import com.vanoma.api.order.utils.Dates;
import com.vanoma.api.utils.NullableValueMapBuilder;
import com.vanoma.api.utils.httpwrapper.HttpResult;
import com.vanoma.api.utils.input.PhoneNumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PackageEventService implements IPackageEventService {
    Logger logger = LoggerFactory.getLogger(PackageEventService.class);

    // TODO: Replace this list with a field in their "preferences" that sellers can update in the dashboard app.
    // CustomerIds of sellers that we should opt-out of sending SMS to their buyers.
    private static Set<String> SELLERS_OPTED_OUT_FROM_BUYER_SMS = Set.of(
            "ebfb2198f9f0479cba67c630705fdba6" // Murukali
    );

    private static Set<EventName> EVENTS_WITH_BUYER_SMS = Set.of(
            EventName.DRIVER_DEPARTING_DROP_OFF,
            EventName.PACKAGE_DELIVERED,
            EventName.PACKAGE_CANCELLED
    );

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private PackageEventRepository packageEventRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ICommunicationApiCaller communicationApiCaller;
    @Autowired
    private IAuthApiCaller authApiCaller;

    @Override
    public PackageEvent createPackageEvent(String packageId, PackageEventJson eventJson) {
        eventJson.validate();
        Package pkg = this.packageRepository.getById(packageId);
        PackageEvent packageEvent = this.createPackageEvent(pkg, eventJson.getEventName(), eventJson.getAssignmentId());
        this.sendPushNotification(pkg, packageEvent);
        return packageEvent;
    }

    @Override
    public PackageEvent createPackageEvent(Package pkg, EventName eventName, String assignmentId) {
        PackageEvent packageEvent = createEvent(pkg, eventName, assignmentId);

        if (eventName == EventName.PACKAGE_DELIVERED) {
            pkg.setStatus(PackageStatus.COMPLETE);
            this.packageRepository.save(pkg);
            this.completeDeliveryOrder(pkg.getDeliveryOrder());
        }

        this.sendSMSNotification(pkg, packageEvent);
        return packageEvent;
    }

    private PackageEvent createEvent(Package pkg, EventName eventName, String assignmentId) {
        PackageEvent existingEvent = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(pkg, eventName, assignmentId);
        if (existingEvent != null) {
            this.packageEventRepository.delete(existingEvent);
        }

        PackageEvent packageEvent = new PackageEvent(pkg)
                .setEventName(eventName)
                .setAssignmentId(assignmentId)
                .setTextEN(EventDescription.getTemplateEN(eventName))
                .setTextFR(EventDescription.getTemplateFR(eventName))
                .setTextRW(EventDescription.getTemplateRW(eventName));

        return this.packageEventRepository.save(packageEvent);
    }

    private void completeDeliveryOrder(DeliveryOrder deliveryOrder) {
        List<Package> packages = this.packageRepository.findByDeliveryOrder(deliveryOrder);

        // Here we are checking for placed status only because if the package for which this event was created is
        // placed (which must be the case), the sibling packages must also be in placed, cancelled or incomplete
        // statuses. Both cancelled and incomplete are final statuses, which leaves with only other placed packages.
        // And so if there's no other placed packages in the order, we can mark it as complete (irrespective of
        // other cancelled and incomplete packages).
        if (packages.stream().allMatch(p -> p.getStatus() != PackageStatus.PLACED)) {
            deliveryOrder.setStatus(OrderStatus.COMPLETE);
            this.orderRepository.save(deliveryOrder);
        }
    }

    private void sendPushNotification(Package pkg, PackageEvent packageEvent) {
        if (pkg.getEnableNotifications()) {
            if (pkg.getEventCallback() != null) {
                CallbackParams params = CallbackParams.builder()
                        .callbackUrl(pkg.getEventCallback())
                        .payload(Map.of(
                                "packageEventId", packageEvent.getPackageEventId(),
                                "eventName", packageEvent.getEventName().name(),
                                "package", objectMapper.convertValue(PackageContacts.create(pkg), Map.class),
                                "createdAt", Dates.stringifyDatetime(packageEvent.getCreatedAt()),
                                "text", Map.of(
                                        "en", packageEvent.getTextEN(),
                                        "fr", packageEvent.getTextFR(),
                                        "rw", packageEvent.getTextRW()
                                )

                        ))
                        .build();
                this.communicationApiCaller.sendCallback(params);
            } else {
                WebPushParams params = WebPushParams.builder()
                        .heading("Check new delivery event!")
                        .message(packageEvent.getTextEN())
                        .receiverIds(List.of(pkg.getFromContact().getCustomerId()))
                        .jsonData(Map.of(
                                "deliveryOrderId", pkg.getDeliveryOrder().getDeliveryOrderId(),
                                "packageEventId", packageEvent.getPackageEventId(),
                                "packageId", pkg.getPackageId(),
                                "eventName", packageEvent.getEventName().name(),
                                "createdAt", Dates.stringifyDatetime(packageEvent.getCreatedAt()),
                                "text", Map.of(
                                        "en", packageEvent.getTextEN(),
                                        "fr", packageEvent.getTextFR(),
                                        "rw", packageEvent.getTextRW()
                                )
                        ))
                        .metadata(new NullableValueMapBuilder<String, String>()
                                .put("appId", System.getenv("WEB_PUSH_CUSTOMER_APP_ID"))
                                .put("apiKey", System.getenv("WEB_PUSH_CUSTOMER_API_KEY"))
                                .build()
                        )
                        .build();
                this.communicationApiCaller.sendWebPush(params);
            }
        }
    }

    private void sendSMSNotification(Package pkg, PackageEvent packageEvent) {
        if (pkg.getEnableNotifications()) {
            String customerId = pkg.getFromContact().getCustomerId();
            Customer customer = this.customerRepository.getById(customerId);

            if (!SELLERS_OPTED_OUT_FROM_BUYER_SMS.contains(customerId)
                    && EVENTS_WITH_BUYER_SMS.contains(packageEvent.getEventName())) {

                if (packageEvent.getEventName() == EventName.DRIVER_DEPARTING_DROP_OFF) {
                    String message = String.format(
                            "You have a delivery from %s (%s). Track the progress at %s",
                            customer.getBusinessName(), PhoneNumberUtils.localize(pkg.getFromContact().getPhoneNumberOne()), pkg.getTrackingLink()
                    );
                    this.communicationApiCaller.sendSMS(message, pkg.getToContact().getPhoneNumberOne());
                }

                if (packageEvent.getEventName() == EventName.PACKAGE_DELIVERED) {
                    String message = String.format(
                            "Your package from %s (%s) is now delivered. Thank you!",
                            customer.getBusinessName(), PhoneNumberUtils.localize(pkg.getFromContact().getPhoneNumberOne())
                    );
                    this.communicationApiCaller.sendSMS(message, pkg.getToContact().getPhoneNumberOne());
                }

                if (packageEvent.getEventName() == EventName.PACKAGE_CANCELLED) {
                    // Only send this SMS if we have sent the buyer an SMS that they have a delivery
                    PackageEvent driverDeparting = this.packageEventRepository.findFirstByPkgAndEventNameAndAssignmentId(
                            pkg, EventName.DRIVER_DEPARTING_DROP_OFF, pkg.getAssignmentId());
                    if (driverDeparting != null) {
                        String message = String.format(
                                "Your package from %s (%s) is cancelled.",
                                customer.getBusinessName(), PhoneNumberUtils.localize(pkg.getFromContact().getPhoneNumberOne())
                        );
                        this.communicationApiCaller.sendSMS(message, pkg.getToContact().getPhoneNumberOne());
                    }
                }
            }
        }
    }
}
