package com.vanoma.api.order.customers;

import com.vanoma.api.order.packages.Package;
import com.vanoma.api.order.utils.EntityFilter;
import com.vanoma.api.order.utils.SpecificationBuilder;
import lombok.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFilter implements EntityFilter<Customer> {
    private List<String> customerId;
    private String businessName;
    private String phoneNumber;
//    private

    @Override
    public boolean isEmpty() {
        return Objects.isNull(customerId)
                && Objects.isNull(businessName)
                && Objects.isNull(phoneNumber);
    }

    @Override
    public Specification<Customer> getSpec() {
        if (isEmpty()) {
            return null;
        }

        SpecificationBuilder<Customer> specs = SpecificationBuilder.builder();

        if (customerId != null) {
            specs.add((Specification<Customer>) (root, query, builder) ->
                    root.get("customerId").in(customerId));
        }

        if (businessName != null) {
            specs.add((Specification<Customer>) (root, query, builder) ->
                    builder.like(root.get("businessName"), "%" + businessName + "%"));
        }

        if (phoneNumber != null) {
            specs.add((Specification<Customer>) (root, query, builder) ->
                    builder.equal(root.get("phoneNumber"), phoneNumber));
        }

        return specs.build();
    }
}
