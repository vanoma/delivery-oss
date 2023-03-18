package com.vanoma.api.order.charges;

import com.vanoma.api.order.orders.DeliveryOrder;
import com.vanoma.api.order.utils.EntityFilter;
import com.vanoma.api.order.utils.SpecificationBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.util.Objects;

@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeFilter implements EntityFilter<Charge> {
    private String packageId;
    private String customerId;
    private String status;

    @Override
    public boolean isEmpty() {
        return Objects.isNull(packageId)
                && Objects.isNull(status)
                && Objects.isNull(customerId);
    }

    @Override
    public Specification<Charge> getSpec() {
        if (isEmpty()) {
            return null;
        }

        SpecificationBuilder<Charge> specs = SpecificationBuilder.builder();

        if (packageId != null) {
            specs.add((Specification<Charge>) (root, query, builder) ->
                    builder.equal(root.get("pkg").get("packageId"), packageId));
        }

        if (status != null) {
            specs.add((Specification<Charge>) (root, query, builder) ->
                    builder.equal(root.get("status"), ChargeStatus.valueOf(status)));
        }

        if (customerId != null) {
            specs.add((Specification<Charge>) (root, query, builder) -> {
                Join<Charge, DeliveryOrder> deliveryOrderJoin = root.join("deliveryOrder");
                return builder.equal(deliveryOrderJoin.get("customerId"), customerId);
            });
        }

        return specs.build();
    }
}
