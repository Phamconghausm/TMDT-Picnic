package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.entity.Order;
import com.java.TMDTPicnic.entity.SharedCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findBySharedCartAndOrderType(SharedCart sharedCart, String orderType);
    // ===== SUMMARY =====
    //COALESCE(a, b, c) if(a=null) return b, b=null return c
    @Query("SELECT COALESCE(SUM(order.totalAmount), 0) FROM Order order")
    Double totalRevenue();

    @Query("SELECT COUNT(order) FROM Order order")
    Long totalOrders();

    @Query("SELECT COUNT(order) FROM Order order WHERE order.status = 'COMPLETED'")
    Long completedOrders();

    // ===== REVENUE BY DAY =====
    @Query("""
        SELECT new com.java.TMDTPicnic.dto.response.RevenueByDayResponse(
            DATE(order.createdAt),
            SUM(order.totalAmount)
        )
        FROM Order order
        GROUP BY DATE(order.createdAt)
        ORDER BY DATE(order.createdAt)
    """)
    List<RevenueByDayResponse> getRevenueByDay();

    // ===== REVENUE BY MONTH =====
    @Query("""
    SELECT new com.java.TMDTPicnic.dto.response.RevenueByMonthResponse(
        CONCAT(
            YEAR(order.createdAt),\s
            '-',
            CASE\s
                WHEN MONTH(o.createdAt) < 10 THEN CONCAT('0', MONTH(order.createdAt))
                ELSE CONCAT('', MONTH(order.createdAt))
            END
        ),
        SUM(order.totalAmount)
    )
    FROM Order order
    GROUP BY YEAR(order.createdAt), MONTH(order.createdAt)
    ORDER BY YEAR(order.createdAt), MONTH(order.createdAt)
""")
    List<RevenueByMonthResponse> getRevenueByMonth();

    // ===== ORDERS BY DAY =====
    @Query("""
        SELECT new com.java.TMDTPicnic.dto.response.OrdersByDayResponse(
            DATE(order.createdAt),
            COUNT(order)
        )
        FROM Order order
        GROUP BY DATE(order.createdAt)
        ORDER BY DATE(order.createdAt)
    """)
    List<OrdersByDayResponse> getOrdersByDay();

    // ===== ORDERS BY MONTH =====
    @Query("""
        SELECT new com.java.TMDTPicnic.dto.response.OrdersByMonthResponse(
            FUNCTION('DATE_FORMAT', order.createdAt, '%Y-%m'),
            COUNT(order)
        )
        FROM Order order
        GROUP BY FUNCTION('DATE_FORMAT', order.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', order.createdAt, '%Y-%m')
    """)
    List<OrdersByMonthResponse> getOrdersByMonth();

    // ===== ORDER STATUS =====
    @Query("""
        SELECT new com.java.TMDTPicnic.dto.response.OrderStatusResponse(
            SUM(CASE WHEN o.status = 'NEW' THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.status = 'PROCESSING' THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END)
        )
        FROM Order o
    """)
    OrderStatusResponse getOrderStatus();
}
