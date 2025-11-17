package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.repository.OrderRepository;
import com.java.TMDTPicnic.repository.ProductRepository;
import com.java.TMDTPicnic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardResponse getDashboardData() {
        DashboardResponse response = new DashboardResponse();

        // Summary
        SummaryResponse summary = new SummaryResponse();
        summary.setTotalRevenue(orderRepository.totalRevenue());
        summary.setTotalOrders(orderRepository.totalOrders());
        summary.setOrdersCompleted(orderRepository.completedOrders());
        summary.setTotalProducts(productRepository.countTotalProducts());
        summary.setProductsSold(productRepository.countTotalSold() != null ? productRepository.countTotalSold() : 0L);
        summary.setTotalUsers(userRepository.countTotalUsers());
        summary.setNewUsers(userRepository.countUsersCreatedThisMonth());
        response.setSummary(summary);

        // Revenue by Day - convert from native query result
        List<Object[]> revenueByDayRaw = orderRepository.getRevenueByDayRaw();
        List<RevenueByDayResponse> revenueByDay = revenueByDayRaw.stream()
                .map(row -> {
                    Date date = (Date) row[0];
                    BigDecimal revenue = row[1] != null ?
                            (row[1] instanceof BigDecimal ? (BigDecimal) row[1] :
                                    new BigDecimal(row[1].toString())) : BigDecimal.ZERO;
                    return new RevenueByDayResponse(date, revenue);
                })
                .collect(Collectors.toList());
        response.setRevenueByDay(revenueByDay);

        // Revenue by Week - convert from native query result
        List<Object[]> revenueByWeekRaw = orderRepository.getRevenueByWeekRaw();
        List<RevenueByWeekResponse> revenueByWeek = revenueByWeekRaw.stream()
                .map(row -> {
                    String week = row[0] != null ? row[0].toString() : "";
                    BigDecimal revenue = row[1] != null ?
                            (row[1] instanceof BigDecimal ? (BigDecimal) row[1] :
                                    new BigDecimal(row[1].toString())) : BigDecimal.ZERO;
                    return new RevenueByWeekResponse(week, revenue);
                })
                .collect(Collectors.toList());
        response.setRevenueByWeek(revenueByWeek);

        // Revenue by Month - convert from native query result
        List<Object[]> revenueByMonthRaw = orderRepository.getRevenueByMonthRaw();
        List<RevenueByMonthResponse> revenueByMonth = revenueByMonthRaw.stream()
                .map(row -> {
                    Date month = (Date) row[0];
                    BigDecimal revenue = row[1] != null ?
                            (row[1] instanceof BigDecimal ? (BigDecimal) row[1] :
                                    new BigDecimal(row[1].toString())) : BigDecimal.ZERO;
                    return new RevenueByMonthResponse(month, revenue);
                })
                .collect(Collectors.toList());
        response.setRevenueByMonth(revenueByMonth);

        // Orders by Day - convert from native query result
        List<Object[]> ordersByDayRaw = orderRepository.getOrdersByDayRaw();
        List<OrdersByDayResponse> ordersByDay = ordersByDayRaw.stream()
                .map(row -> {
                    // Convert java.sql.Date to LocalDate
                    Date sqlDate = (Date) row[0];
                    LocalDate date = sqlDate.toLocalDate();
                    Long orders = row[1] != null ?
                            (row[1] instanceof Long ? (Long) row[1] :
                                    Long.valueOf(row[1].toString())) : 0L;
                    return new OrdersByDayResponse(date, orders);
                })
                .collect(Collectors.toList());
        response.setOrdersByDay(ordersByDay);

        // Orders by Week - convert from native query result
        List<Object[]> ordersByWeekRaw = orderRepository.getOrdersByWeekRaw();
        List<OrdersByWeekResponse> ordersByWeek = ordersByWeekRaw.stream()
                .map(row -> {
                    String week = row[0] != null ? row[0].toString() : "";
                    Long orders = row[1] != null ?
                            (row[1] instanceof Long ? (Long) row[1] :
                                    Long.valueOf(row[1].toString())) : 0L;
                    return new OrdersByWeekResponse(week, orders);
                })
                .collect(Collectors.toList());
        response.setOrdersByWeek(ordersByWeek);

        // Orders by Month - convert from native query result
        List<Object[]> ordersByMonthRaw = orderRepository.getOrdersByMonthRaw();
        List<OrdersByMonthResponse> ordersByMonth = ordersByMonthRaw.stream()
                .map(row -> {
                    String month = row[0] != null ? row[0].toString() : "";
                    Long orders = row[1] != null ?
                            (row[1] instanceof Long ? (Long) row[1] :
                                    Long.valueOf(row[1].toString())) : 0L;
                    return new OrdersByMonthResponse(month, orders);
                })
                .collect(Collectors.toList());
        response.setOrdersByMonth(ordersByMonth);

        // User Stats by Day - convert from native query result
        List<Object[]> userStatsByDayRaw = userRepository.getUserStatsByDayRaw();
        List<UserStatsByDayResponse> userStatsByDay = userStatsByDayRaw.stream()
                .map(row -> {
                    Date date = (Date) row[0];
                    Long newUsers = row[1] != null ?
                            (row[1] instanceof Long ? (Long) row[1] :
                                    Long.valueOf(row[1].toString())) : 0L;
                    Long returningUsers = row[2] != null ?
                            (row[2] instanceof Long ? (Long) row[2] :
                                    Long.valueOf(row[2].toString())) : 0L;
                    return new UserStatsByDayResponse(date, newUsers, returningUsers);
                })
                .collect(Collectors.toList());
        response.setUserStatsByDay(userStatsByDay);

        // Top Categories
        List<TopCategoryResponse> topCategories = productRepository.getTopCategories();
        response.setTopCategories(topCategories);
        List<TopCategoryResponse> topCategoriesTop3 = topCategories.stream()
                .limit(3)
                .collect(Collectors.toList());
        response.setTopCategoriesTop3(topCategoriesTop3);

        // Top Products - limit to top 10
        List<TopProductResponse> topProducts = productRepository.getTopProducts();
        List<TopProductResponse> topProductsTop3 = topProducts.stream()
                .limit(3)
                .collect(Collectors.toList());
        if (topProducts.size() > 10) {
            topProducts = topProducts.subList(0, 10);
        }
        response.setTopProducts(topProducts);
        response.setTopProductsTop3(topProductsTop3);

        // Order Status
        response.setOrderStatus(orderRepository.getOrderStatus());

        return response;
    }
}

