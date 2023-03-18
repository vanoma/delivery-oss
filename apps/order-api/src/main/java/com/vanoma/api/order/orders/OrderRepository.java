package com.vanoma.api.order.orders;

import com.vanoma.api.order.customers.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<DeliveryOrder, String> {

    String CUSTOMER_UNPAID_DELIVERY_ORDERS = "select * from delivery_order d " +
            "inner join charge c on d.delivery_order_id = c.delivery_order_id " +
            "where d.customer_id = :customerId and d.status = 'COMPLETE' and c.status = 'UNPAID' ";

    String GLOBAL_UNPAID_DELIVERY_ORDERS = "select * from delivery_order d " +
            "inner join charge c on d.delivery_order_id = c.delivery_order_id " +
            "where d.status = :orderStatus and c.status = :paymentStatus " +
            "group by d.delivery_order_id";

    String CUSTOMERS_DELIVERY_ORDERS_DAILY = "select customer_id, count(*), " +
            "DATE(placed_at) date_placed from delivery_order " +
            "where status = :orderStatus group by customer_id, date_placed";

    String CUSTOMERS_DELIVERY_ORDERS_MONTHLY = "select customer_id, count(*), " +
            "YEAR(placed_at) placed_year, MONTH(placed_at) placed_month " +
            "from delivery_order " +
            "where status = :orderStatus " +
            "group by customer_id, placed_year, placed_month";

    String GROUP_BY_DELIVERY_ORDER_ID = " group by d.delivery_order_id ";
    String ORDER_BY_PLACED_AT = " order by d.placed_at desc";

    Page<DeliveryOrder> findByCustomerIdAndStatus(String customerId, OrderStatus orderStatus, Pageable pageable);

    long countByStatus(OrderStatus orderStatus);

    Page<DeliveryOrder> findByStatus(OrderStatus orderStatus, Pageable pageable);

    @Query(value = "SELECT * FROM delivery_order d " +
            "JOIN package pkg ON d.delivery_order_id=pkg.delivery_order_id " +
            "JOIN contact co ON pkg.from_contact = co.contact_id OR pkg.to_contact = co.contact_id " +
            "WHERE d.status=:orderStatus AND co.phone_number_one LIKE :phoneNumber% " +
            "GROUP BY d.delivery_order_id",
            nativeQuery = true)
    Page<DeliveryOrder> findByStatusAndPackagesWithContactPhoneNumber(
            @Param("orderStatus") String orderStatus,
            @Param("phoneNumber") String phoneNumber,
            Pageable pageable);

    @Query(value = CUSTOMER_UNPAID_DELIVERY_ORDERS +
            "and d.placedAt > :placedAfter and d.placedAt < :placedBefore " +
            GROUP_BY_DELIVERY_ORDER_ID + ORDER_BY_PLACED_AT,
            nativeQuery = true)
    List<DeliveryOrder> getUnpaidDeliveryOrders(@Param("customerId") String customerId,
                                                @Param("placedAfter") OffsetDateTime placedAfter,
                                                @Param("placedBefore") OffsetDateTime placedBefore);

    @Query(value = CUSTOMER_UNPAID_DELIVERY_ORDERS +
            "and d.placedAt > :placedAfter " +
            GROUP_BY_DELIVERY_ORDER_ID + ORDER_BY_PLACED_AT,
            nativeQuery = true)
    List<DeliveryOrder> getUnpaidDeliveryOrdersPlacedAfter(@Param("customerId") String customerId,
                                                           @Param("placedAfter") OffsetDateTime placedAfter);

    @Query(value = CUSTOMER_UNPAID_DELIVERY_ORDERS +
            "and d.placed_at < :placedBefore " +
            GROUP_BY_DELIVERY_ORDER_ID + ORDER_BY_PLACED_AT,
            nativeQuery = true)
    List<DeliveryOrder> getUnpaidDeliveryOrdersPlacedBefore(@Param("customerId") String customerId,
                                                            @Param("placedBefore") OffsetDateTime placedBefore);

    @Query(value = CUSTOMER_UNPAID_DELIVERY_ORDERS + GROUP_BY_DELIVERY_ORDER_ID + ORDER_BY_PLACED_AT,
            nativeQuery = true)
    List<DeliveryOrder> getUnpaidDeliveryOrders(@Param("customerId") String customerId);

    @Query(value = GLOBAL_UNPAID_DELIVERY_ORDERS,
            nativeQuery = true)
    Page<DeliveryOrder> findByOrderStatusAndPaymentStatus(
            @Param("orderStatus") String orderStatus, @Param("paymentStatus") String paymentStatus,
            Pageable pageable
    );

    @Query(value = CUSTOMERS_DELIVERY_ORDERS_DAILY, nativeQuery = true)
    List<Object[]> getDailyOrdersPerCustomer(@Param("orderStatus") String orderStatus);

    @Query(value = CUSTOMERS_DELIVERY_ORDERS_MONTHLY, nativeQuery = true)
    List<Object[]> getMonthlyOrdersPerCustomer(@Param("orderStatus") String orderStatus);

    List<DeliveryOrder> findByCustomer(Customer customer);
}
