package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.entity.Order;
import com.java.TMDTPicnic.entity.SharedCart;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findBySharedCartAndOrderType(SharedCart sharedCart, String orderType);
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    // ===== ORDER HISTORY =====
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserAndOrderTypeIsOrderByCreatedAtDesc(User user, String orderType);
    List<Order> findByUserAndOrderTypeIsNotOrderByCreatedAtDesc(User user, String orderType);

    // ===== SUMMARY =====
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal totalRevenue();

    @Query("SELECT COUNT(o) FROM Order o")
    Long totalOrders();

    @Query("""
    SELECT new com.java.TMDTPicnic.dto.response.OrderStatusResponse(
        SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN o.status = 'PAID' THEN 1 ELSE 0 END)
    )
    FROM Order o
    """)
    OrderStatusResponse getOrderStatus();


    // ===== REVENUE BY DAY =====
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COALESCE(SUM(total_amount), 0) as revenue
        FROM orders
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueByDayRaw();

    // ===== REVENUE BY WEEK =====
    @Query(value = """
        SELECT 
            CONCAT(YEAR(created_at), '-W', LPAD(WEEK(created_at, 3), 2, '0')) AS week,
            COALESCE(SUM(total_amount), 0) AS revenue
        FROM orders
        GROUP BY CONCAT(YEAR(created_at), '-W', LPAD(WEEK(created_at, 3), 2, '0'))
        ORDER BY MIN(created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueByWeekRaw();

    // ===== REVENUE BY MONTH =====
    @Query(value = """
        SELECT 
            STR_TO_DATE(CONCAT(YEAR(created_at), '-', LPAD(MONTH(created_at), 2, '0'), '-01'), '%Y-%m-%d') AS month,
            COALESCE(SUM(total_amount), 0) AS revenue
        FROM orders
        GROUP BY STR_TO_DATE(CONCAT(YEAR(created_at), '-', LPAD(MONTH(created_at), 2, '0'), '-01'), '%Y-%m-%d')
        ORDER BY MIN(created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueByMonthRaw();

    // ===== ORDERS BY MONTH =====
    @Query(value = """
        SELECT 
            DATE_FORMAT(created_at, '%Y-%m') as month,
            COUNT(*) as orders
        FROM orders
        GROUP BY DATE_FORMAT(created_at, '%Y-%m')
        ORDER BY DATE_FORMAT(created_at, '%Y-%m')
    """, nativeQuery = true)
    List<Object[]> getOrdersByMonthRaw();

    // ===== ORDER STATUS =====
//    @Query("""
//        SELECT new com.java.TMDTPicnic.dto.response.OrderStatusResponse(
//            SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END)
//        )
//        FROM Order o
//    """)
//    OrderStatusResponse getOrderStatus();

    // ===== REVENUE BY DAY WITH DATE RANGE =====
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COALESCE(SUM(total_amount), 0) as revenue
        FROM orders
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueByDayRawWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // ===== REVENUE BY WEEK WITH DATE RANGE =====
    @Query(value = """
        SELECT 
            CONCAT(YEAR(created_at), '-W', LPAD(WEEK(created_at, 3), 2, '0')) AS week,
            COALESCE(SUM(total_amount), 0) AS revenue
        FROM orders
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
        GROUP BY CONCAT(YEAR(created_at), '-W', LPAD(WEEK(created_at, 3), 2, '0'))
        ORDER BY MIN(created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueByWeekRawWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // ===== ORDERS BY DAY WITH DATE RANGE =====
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as orders
        FROM orders
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getOrdersByDayRawWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // ===== ORDERS BY WEEK WITH DATE RANGE =====
    @Query(value = """
        SELECT 
            CONCAT(YEAR(created_at), '-W', LPAD(WEEK(created_at, 3), 2, '0')) as week,
            COUNT(*) as orders
        FROM orders
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
        GROUP BY CONCAT(YEAR(created_at), '-W', LPAD(WEEK(created_at, 3), 2, '0'))
        ORDER BY MIN(created_at)
    """, nativeQuery = true)
    List<Object[]> getOrdersByWeekRawWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // ===== TOTAL REVENUE WITH DATE RANGE =====
    @Query(value = """
        SELECT COALESCE(SUM(total_amount), 0)
        FROM orders
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
    """, nativeQuery = true)
    Double getTotalRevenueWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // ===== TOTAL ORDERS WITH DATE RANGE =====
    @Query(value = """
        SELECT COUNT(*)
        FROM orders
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
    """, nativeQuery = true)
    Long getTotalOrdersWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

}
