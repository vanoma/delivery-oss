package com.vanoma.api.order.orders;

public class OrderUtils {
    public static boolean isOrderUpdatable(OrderStatus orderStatus) {
        return orderStatus == OrderStatus.STARTED || orderStatus == OrderStatus.REQUEST;
    }
}
