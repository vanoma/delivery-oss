package com.vanoma.api.order.orders;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DiscountUtils {
    public static Set<Discount> getPendingDiscount(List<DeliveryOrder> deliveryOrders) {
        return deliveryOrders
                .stream()
                .map(DeliveryOrder::getPendingDiscounts)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
