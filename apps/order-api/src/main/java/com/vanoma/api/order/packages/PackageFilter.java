package com.vanoma.api.order.packages;

import com.vanoma.api.order.charges.Charge;
import com.vanoma.api.order.charges.ChargeStatus;
import com.vanoma.api.order.contacts.Contact;
import com.vanoma.api.order.customers.Customer;
import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.utils.EntityFilter;
import com.vanoma.api.order.utils.SpecificationBuilder;
import lombok.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageFilter implements EntityFilter<Package> {
    private List<String> packageId;
    private List<String> status;
    private String trackingNumber;
    private String phoneNumber;
    private String customerId;
    private String deliveryOrderId;
    private String paymentStatus;
    private String isAssigned;
    private String isAssignable;
    private String branchId;

    @Override
    public boolean isEmpty() {
        return Objects.isNull(packageId)
                && Objects.isNull(status)
                && Objects.isNull(trackingNumber)
                && Objects.isNull(phoneNumber)
                && Objects.isNull(customerId)
                && Objects.isNull(deliveryOrderId)
                && Objects.isNull(paymentStatus)
                && Objects.isNull(isAssigned)
                && Objects.isNull(isAssignable)
                && Objects.isNull(branchId);
    }

    @Override
    public Specification<Package> getSpec() {
        if (isEmpty()) {
            return null;
        }

        SpecificationBuilder<Package> specs = SpecificationBuilder.builder();

        if (packageId != null) {
            specs.add((Specification<Package>) (root, query, builder) ->
                    root.get("packageId").in(packageId));
        }

        if (status != null) {
            specs.add((Specification<Package>) (root, query, builder) ->
                    root.get("status").in(status.stream().map(PackageStatus::create).collect(Collectors.toList())));
        }

        if (trackingNumber != null) {
            specs.add((Specification<Package>) (root, query, builder) ->
                    builder.like(root.get("trackingNumber"), trackingNumber + "%"));
        }

        if (phoneNumber != null) {
            specs.add((Specification<Package>) (root, query, builder) -> {
                // Idea borrowed from https://www.baeldung.com/jpa-criteria-api-in-expressions
                Subquery<Contact> contactSubquery = query.subquery(Contact.class);
                Root<Contact> contactRoot = contactSubquery.from(Contact.class);
                contactSubquery.select(contactRoot)
                        .distinct(true)
                        .where(builder.equal(contactRoot.get("phoneNumberOne"), phoneNumber));

                Subquery<Customer> customerSubquery = query.subquery(Customer.class);
                Root<Customer> customerRoot = customerSubquery.from(Customer.class);
                customerSubquery.select(customerRoot)
                        .distinct(true)
                        .where(builder.equal(customerRoot.get("phoneNumber"), phoneNumber));

                return builder.or(
                        root.get("fromContact").in(contactSubquery),
                        root.get("toContact").in(contactSubquery),
                        root.join("deliveryOrder").get("customer").get("customerId").in(customerSubquery));
            });
        }

        if (customerId != null) {
            specs.add((Specification<Package>) (root, query, builder) -> {
                Join<Package, DeliveryOrder> deliveryOrderJoin = root.join("deliveryOrder");
                return builder.equal(deliveryOrderJoin.get("customer").get("customerId"), customerId);
            });
        }

        if (deliveryOrderId != null) {
            specs.add((Specification<Package>) (root, query, builder) ->
                    builder.equal(root.get("deliveryOrder").get("deliveryOrderId"), deliveryOrderId));
        }

        if (paymentStatus != null) {
            specs.add((Specification<Package>) (root, query, builder) -> {
                Join<Package, Charge> chargeJoin = root.join("charges");
                return builder.equal(chargeJoin.get("status"), ChargeStatus.create(paymentStatus));
            });
        }

        if (this.isAssigned != null) {
            boolean isAssigned = Boolean.parseBoolean(this.isAssigned);

            if (isAssigned) {
                specs.add((Specification<Package>) (root, query, builder) ->
                        builder.isNotNull(root.get("driverId")));
            } else {
                specs.add((Specification<Package>) (root, query, builder) ->
                        builder.isNull(root.get("driverId")));
            }
        }

        if (this.isAssignable != null) {
            boolean isAssignable = Boolean.parseBoolean(this.isAssignable);
            specs.add((Specification<Package>) (root, query, builder) ->
                    builder.equal(root.get("isAssignable"), isAssignable));
        }

        if (branchId != null) {
            specs.add((Specification<Package>) (root, query, builder) -> {
                Join<Package, DeliveryOrder> deliveryOrderJoin = root.join("deliveryOrder");
                return builder.equal(deliveryOrderJoin.get("branch").get("branchId"), branchId);
            });
        }

        return specs.build();
    }
}
