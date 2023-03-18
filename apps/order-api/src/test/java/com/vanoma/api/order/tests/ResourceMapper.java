package com.vanoma.api.order.tests;

import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeUtils;
import com.vanoma.api.order.contacts.Address;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.customers.Agent;
import com.vanoma.api.order.customers.Branch;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.events.PackageEvent;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.orders.Discount;
import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.packages.PackageContacts;
import com.vanoma.api.order.pricing.CustomPricing;
import com.vanoma.api.utils.NullableValueMapBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A utility class providing the java representation of how each entity looks when it's serialized
 * in JSON format. The attributes in each map are exhaustive to provide an idea of what the API will
 * return to the client.
 */
public class ResourceMapper {
    public static Map<String, Object> createCustomerMap(Customer customer) {
        return new NullableValueMapBuilder<String, Object>()
                .put("customerId", customer.getCustomerId())
                .put("businessName", customer.getBusinessName())
                .put("phoneNumber", customer.getPhoneNumber())
                .put("weightingFactor", serializeBigDecimal(customer.getWeightingFactor()))
                .put("billingInterval", customer.getBillingInterval())
                .put("billingGracePeriod", customer.getBillingGracePeriod())
                .put("postpaidExpiry", stringifyDateTime(customer.getPostpaidExpiry()))
                .put("fixedPriceAmount", serializeBigDecimal(customer.getFixedPriceAmount()))
                .put("fixedPriceExpiry", stringifyDateTime(customer.getFixedPriceExpiry()))
                .put("isPrepaid", customer.getIsPrepaid())
                .put("hasFixedPrice", customer.getHasFixedPrice())
                .put("createdAt", stringifyDateTime(customer.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(customer.getUpdatedAt()))
                .build();
    }

    public static Map<String, Object> createBranchMap(Branch branch) {
        return new NullableValueMapBuilder<String, Object>()
                .put("branchId", branch.getBranchId())
                .put("branchName", branch.getBranchName())
                .put("isDeleted", branch.getIsDeleted())
                .put("contact", createContactMap(branch.getContact()))
                .put("address", createAddressMap(branch.getAddress()))
                .put("createdAt", stringifyDateTime(branch.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(branch.getUpdatedAt()))
                .build();
    }

    public static Map<String, Object> createAgentMap(Agent agent) {
        Map<String, Object> branchMap = Objects.nonNull(agent.getBranch())
                ? ResourceMapper.createBranchMap(agent.getBranch())
                : null;

        return new NullableValueMapBuilder<String, Object>()
                .put("agentId", agent.getAgentId())
                .put("fullName", agent.getFullName())
                .put("phoneNumber", agent.getPhoneNumber())
                .put("isRoot", agent.getIsRoot())
                .put("isDeleted", agent.getIsDeleted())
                .put("branch", branchMap)
                .put("createdAt", stringifyDateTime(agent.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(agent.getUpdatedAt()))
                .build();
    }

    public static Map<String, Object> createDeliveryOrderMap(DeliveryOrder order) {
        return createDeliveryOrderMap(order, false);
    }

    public static Map<String, Object> createDeliveryOrderMap(DeliveryOrder order, boolean asApiUser) {
        NullableValueMapBuilder<String, Object> builder = new NullableValueMapBuilder<String, Object>()
                .put("deliveryOrderId", order.getDeliveryOrderId())
                .put("customerId", order.getCustomerId())
                .put("placedAt", stringifyDateTime(order.getPlacedAt()))
                .put("isCustomerPaying", order.getIsCustomerPaying())
                .put("clientType", order.getClientType().name())
                .put("status", order.getStatus().name())
                .put("deliveryLink", order.getDeliveryLink())
                .put("linkOpenedAt", stringifyDateTime(order.getLinkOpenedAt()));

        if (asApiUser) {
            List<Map<String, Object>> packagesMapList = order.getPackages().
                    stream()
                    .map(pkg -> createPackageMap(pkg, true)).collect(Collectors.toList());

            return builder
                    .put("packages", packagesMapList)
                    .build();
        }

        return builder
                .put("customer", createCustomerMap(order.getCustomer()))
                .put("branch", Objects.isNull(order.getBranch()) ? null : createBranchMap(order.getBranch()))
                .put("agent", Objects.isNull(order.getAgent()) ? null : createAgentMap(order.getAgent()))
                .put("createdAt", stringifyDateTime(order.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(order.getUpdatedAt()))
                .build();
    }

    public static Map<String, Object> createPackageMap(Package pkg) {
        return createPackageMap(pkg, false);
    }

    public static Map<String, Object> createPackageMap(Package pkg, boolean asApiUser) {
        NullableValueMapBuilder<String, Object> builder = new NullableValueMapBuilder<String, Object>()
                .put("packageId", pkg.getPackageId())
                .put("priority", pkg.getPriority().name())
                .put("trackingNumber", pkg.getTrackingNumber())
                .put("trackingLink", pkg.getTrackingLink())
                .put("size", pkg.getSize().name())
                .put("status", pkg.getStatus().name())
                .put("fromContact", ResourceMapper.createContactMap(pkg.getFromContact()))
                .put("toContact", ResourceMapper.createContactMap(pkg.getToContact()))
                .put("fromAddress", ResourceMapper.createAddressMap(pkg.getFromAddress()))
                .put("toAddress", ResourceMapper.createAddressMap(pkg.getToAddress()))
                .put("fromNote", pkg.getFromNote())
                .put("toNote", pkg.getToNote())
                .put("pickUpStart", stringifyDateTime(pkg.getPickUpStart()))
                .put("pickUpEnd", stringifyDateTime(pkg.getPickUpEnd()))
                .put("fragileContent", pkg.getFragileContent())
                .put("eventCallback", pkg.getEventCallback());

        if (asApiUser) {
            return builder.build();
        }

        return builder
                .put("deliveryOrder", ResourceMapper.createDeliveryOrderMap(pkg.getDeliveryOrder()))
                .put("driverId", pkg.getDriverId())
                .put("assignmentId", pkg.getAssignmentId())
                .put("createdAt", stringifyDateTime(pkg.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(pkg.getUpdatedAt()))
                .put("paymentStatus", pkg.getPaymentStatus().name())
                .put("transactionAmount", serializeBigDecimal(pkg.getTransactionAmount()))
                .put("transactionFee", serializeBigDecimal(pkg.getTransactionFee()))
                .put("totalAmount", serializeBigDecimal(pkg.getTotalAmount()))
                .put("staffNote", pkg.getStaffNote())
                .put("isAssignable", pkg.getIsAssignable())
                .put("isExpress", pkg.getIsExpress())
                .put("pickUpChangeNote", pkg.getPickUpChangeNote())
                .put("cancellationNote", pkg.getCancellationNote())
                .put("enableNotifications", pkg.getEnableNotifications())
                .put("events", pkg.getEvents().stream().map(ResourceMapper::createPackageEventMap).collect(Collectors.toList()))
                .build();
    }

    public static Map<String, Object> createPackageContactsMap(Package pkg) {
        PackageContacts pkgContacts = PackageContacts.create(pkg);
        return new NullableValueMapBuilder<String, Object>()
                .put("packageId", pkgContacts.getPackageId())
                .put("deliveryOrderId", pkgContacts.getDeliveryOrderId())
                .put("fromContact", ResourceMapper.createContactPreviewMap(pkgContacts.getFromContact()))
                .put("toContact", ResourceMapper.createContactPreviewMap(pkgContacts.getToContact()))
                .build();
    }

    private static Map<String, Object> createContactPreviewMap(PackageContacts.ContactPreview contactPreview) {
        return new NullableValueMapBuilder<String, Object>()
                .put("name", contactPreview.getName())
                .put("phoneNumberOne", contactPreview.getPhoneNumberOne())
                .build();
    }

    public static Map<String, Object> createPackageEventMap(PackageEvent packageEvent) {
        return new NullableValueMapBuilder<String, Object>()
                .put("packageEventId", packageEvent.getPackageEventId())
                .put("packageId", packageEvent.getPackageId())
                .put("deliveryOrderId", packageEvent.getDeliveryOrderId())
                .put("assignmentId", packageEvent.getAssignmentId())
                .put("eventName", packageEvent.getEventName().name())
                .put("textEN", packageEvent.getTextEN())
                .put("textFR", packageEvent.getTextFR())
                .put("textRW", packageEvent.getTextRW())
                .put("createdAt", stringifyDateTime(packageEvent.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(packageEvent.getUpdatedAt()))
                .build();
    }

    public static Map<String, Object> createContactMap(Contact contact) {
        return new NullableValueMapBuilder<String, Object>()
                .put("contactId", contact.getContactId())
                .put("customerId", contact.getCustomerId())
                .put("name", contact.getName())
                .put("phoneNumberOne", contact.getPhoneNumberOne())
                .put("phoneNumberTwo", contact.getPhoneNumberTwo())
                .put("isSaved", contact.getIsSaved())
                .put("isDefault", contact.getIsDefault())
                .put("parentContactId", contact.getParentContactId())
                .put("createdAt", stringifyDateTime(contact.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(contact.getUpdatedAt()))
                .build();
    }

    public static Map<String, Object> createAddressMap(Address address) {
        return new NullableValueMapBuilder<String, Object>()
                .put("addressId", address.getAddressId())
                .put("customerId", address.getCustomerId())
                .put("addressName", address.getAddressName())
                .put("houseNumber", address.getHouseNumber())
                .put("streetName", address.getStreetName())
                .put("apartmentNumber", address.getApartmentNumber())
                .put("floor", address.getFloor())
                .put("room", address.getRoom())
                .put("district", address.getDistrict().name())
                .put("latitude", address.getLatitude())
                .put("longitude", address.getLongitude())
                .put("coordinates", Map.of(
                        "type", "Point",
                        "coordinates", List.of(address.getCoordinates().getCoordinate().getX(), address.getCoordinates().getCoordinate().getY())
                ))
                .put("placeName", address.getPlaceName())
                .put("landmark", address.getLandmark())
                .put("isDefault", address.getIsDefault())
                .put("isSaved", address.getIsSaved())
                .put("isConfirmed", address.getIsConfirmed())
                .put("parentAddressId", address.getParentAddressId())
                .put("createdAt", stringifyDateTime(address.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(address.getUpdatedAt()))
                .build();
    }

    public static Map<String, Object> createChargeMap(Charge charge) {
        return new NullableValueMapBuilder<String, Object>()
                .put("chargeId", charge.getChargeId())
                .put("packageId", charge.getPackageId())
                .put("deliveryOrderId", charge.getDeliveryOrderId())
                .put("type", charge.getType().name())
                .put("status", charge.getStatus().name())
                .put("transactionAmount", roundBigDecimal(charge.getTransactionAmount()))
                .put("actualTransactionAmount", roundBigDecimal(charge.getActualTransactionAmount()))
                .put("transactionFee", roundBigDecimal(charge.getTransactionFee()))
                .put("totalAmount", roundBigDecimal(charge.getTotalAmount()))
                .put("description", charge.getDescription())
                .put("createdAt", stringifyDateTime(charge.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(charge.getUpdatedAt()))
                .build();
    }

    public static Map<String, Object> createCustomPricingMap(CustomPricing customPricing) {
        return new NullableValueMapBuilder<String, Object>()
                .put("customPricingId", customPricing.getCustomPricingId())
                .put("customerId", customPricing.getCustomerId())
                .put("customerName", customPricing.getCustomerName())
                .put("active", customPricing.isActive())
                .put("price", ChargeUtils.roundBigDecimal(customPricing.getPrice()))
                .put("expireAt", stringifyDateTime(customPricing.getExpireAt()))
                .put("createdAt", stringifyDateTime(customPricing.getCreatedAt()))
                .build();
    }

    public static Map<String, Object> createDiscountMap(Discount discount) {
        return new NullableValueMapBuilder<String, Object>()
                .put("discountId", discount.getDiscountId())
                .put("type", discount.getType().name())
                .put("amount", ChargeUtils.roundBigDecimal(discount.getAmount()))
                .put("createdAt", stringifyDateTime(discount.getCreatedAt()))
                .put("updatedAt", stringifyDateTime(discount.getUpdatedAt()))
                .build();
    }

    public static String stringifyDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        // For some reasons, the API omits the trailing zero but dateTime.toString() includes it. This is
        // a workaround to remove it. Ideally, we should find a way to serializer OffsetDateTime similar to
        // how the API does it. Use recursion to iteratively remove trailing zeros.
        return stringifyDatetime(dateTime.toString());
    }

    private static String stringifyDatetime(String value) {
        return value.endsWith("0Z") ? stringifyDatetime(value.replace("0Z", "Z")) : value;
    }

    private static Object serializeBigDecimal(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }

        return bigDecimal.doubleValue();
    }

    private static Object roundBigDecimal(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }

        return ChargeUtils.roundBigDecimal(bigDecimal);
    }
}
