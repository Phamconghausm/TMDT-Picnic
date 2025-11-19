package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.DashboardRequest;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.repository.OrderRepository;
import com.java.TMDTPicnic.repository.ProductRepository;
import com.java.TMDTPicnic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    // ==================== DASHBOARD WITH DATE RANGE ====================
    public DashboardResponse getDashboard(DashboardRequest request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();

        logger.info("DashboardService.getDashboard called with fromDate={}, toDate={}", fromDate, toDate);

        DashboardResponse response = new DashboardResponse();

        try {
            logger.info("Building summary...");
            response.setSummary(buildSummary(fromDate, toDate));
            logger.info("Summary built: {}", response.getSummary());

            logger.info("Loading revenueByDay...");
            var revDayRaw = orderRepository.getRevenueByDayRawWithDateRange(fromDate, toDate);
            logger.info("RevenueByDay raw size: {}", revDayRaw.size());
            response.setRevenueByDay(mapToRevenueByDay(revDayRaw));

            logger.info("Loading revenueByWeek...");
            var revWeekRaw = orderRepository.getRevenueByWeekRawWithDateRange(fromDate, toDate);
            logger.info("RevenueByWeek raw size: {}", revWeekRaw.size());
            response.setRevenueByWeek(mapToRevenueByWeek(revWeekRaw));

            logger.info("Loading revenueByMonth...");
            var revMonthRaw = orderRepository.getRevenueByMonthRaw();
            logger.info("RevenueByMonth raw size: {}", revMonthRaw.size());
            var revMonthMapped = mapToRevenueByMonth(revMonthRaw);
            logger.info("RevenueByMonth mapped size: {}", revMonthMapped.size());
            var filteredRevMonth = filterRevenueByMonth(revMonthMapped, fromDate, toDate);
            logger.info("RevenueByMonth filtered size: {}", filteredRevMonth.size());
            response.setRevenueByMonth(filteredRevMonth);

            logger.info("Loading ordersByDay...");
            var ordDayRaw = orderRepository.getOrdersByDayRawWithDateRange(fromDate, toDate);
            logger.info("OrdersByDay raw size: {}", ordDayRaw.size());
            response.setOrdersByDay(mapToOrdersByDay(ordDayRaw));

            logger.info("Loading ordersByWeek...");
            var ordWeekRaw = orderRepository.getOrdersByWeekRawWithDateRange(fromDate, toDate);
            logger.info("OrdersByWeek raw size: {}", ordWeekRaw.size());
            response.setOrdersByWeek(mapToOrdersByWeek(ordWeekRaw));

            logger.info("Loading ordersByMonth...");
            var ordMonthRaw = orderRepository.getOrdersByMonthRaw();
            logger.info("OrdersByMonth raw size: {}", ordMonthRaw.size());
            response.setOrdersByMonth(mapToOrdersByMonth(ordMonthRaw));

            logger.info("Loading userStatsByDay...");
            var userStatsRaw = userRepository.getUserStatsByDayRawWithDateRange(fromDate, toDate);
            logger.info("UserStatsByDay raw size: {}", userStatsRaw.size());
            response.setUserStatsByDay(mapToUserStatsByDay(userStatsRaw));

            setTopCategories(response);
            setTopProducts(response);

            response.setOrderStatus(orderRepository.getOrderStatus());

            logger.info("DashboardResponse built successfully");
            return response;
        } catch (Exception e) {
            logger.error("Error building dashboard response", e);
            throw e;
        }
    }


    // ==================== SUMMARY BUILDER ====================
    private SummaryResponse buildSummary(LocalDate fromDate, LocalDate toDate) {
        SummaryResponse summary = new SummaryResponse();

        if (fromDate == null || toDate == null) {
            summary.setTotalRevenue(orderRepository.totalRevenue());
            summary.setTotalOrders(orderRepository.totalOrders());
            summary.setTotalUsers(userRepository.countTotalUsers());
        } else {
            summary.setTotalRevenue(
                    safeBigDecimal(orderRepository.getTotalRevenueWithDateRange(fromDate, toDate)));
            summary.setTotalOrders(orderRepository.getTotalOrdersWithDateRange(fromDate, toDate));
            summary.setTotalUsers(userRepository.getTotalUsersWithDateRange(toDate));
            summary.setNewUsers(userRepository.getNewUsersWithDateRange(fromDate, toDate));
        }

        summary.setOrdersCompleted(orderRepository.getOrderStatus());
        summary.setTotalProducts(productRepository.countTotalProducts());
        summary.setProductsSold(safeLong(productRepository.countTotalSold()));

        return summary;
    }

    // ==================== TOP ITEMS SETTER ====================
    private void setTopCategories(DashboardResponse response) {
        List<TopCategoryResponse> categories = productRepository.getTopCategories();
        response.setTopCategories(categories);
        response.setTopCategoriesTop3(categories.stream().limit(3).collect(Collectors.toList()));
    }

    private void setTopProducts(DashboardResponse response) {
        List<TopProductResponse> products = productRepository.getTopProducts();
        response.setTopProducts(products.size() > 10 ? products.subList(0, 10) : products);
        response.setTopProductsTop3(products.stream().limit(3).collect(Collectors.toList()));
    }

    // ==================== MAPPERS ====================
    private List<RevenueByDayResponse> mapToRevenueByDay(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new RevenueByDayResponse(
                        (Date) row[0],
                        safeBigDecimal(row[1])
                ))
                .collect(Collectors.toList());
    }

    private List<RevenueByWeekResponse> mapToRevenueByWeek(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new RevenueByWeekResponse(
                        row[0] != null ? row[0].toString() : "",
                        safeBigDecimal(row[1])
                ))
                .collect(Collectors.toList());
    }

    private List<RevenueByMonthResponse> mapToRevenueByMonth(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new RevenueByMonthResponse(
                        (Date) row[0],
                        safeBigDecimal(row[1])
                ))
                .collect(Collectors.toList());
    }

    private List<RevenueByMonthResponse> filterRevenueByMonth(List<RevenueByMonthResponse> list, LocalDate fromDate, LocalDate toDate) {
        return list.stream()
                .filter(r -> {
                    if (r.getMonth() == null) {
                        logger.warn("RevenueByMonthResponse with null month found, skipping");
                        return false;
                    }
                    try {
                        LocalDate monthDate = r.getMonth()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        return !monthDate.isBefore(fromDate.withDayOfMonth(1)) && !monthDate.isAfter(toDate);
                    } catch (Exception e) {
                        logger.error("Error converting month date in filterRevenueByMonth", e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }


    private List<OrdersByDayResponse> mapToOrdersByDay(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new OrdersByDayResponse(
                        ((Date) row[0]).toLocalDate(),
                        safeLong(row[1])
                ))
                .collect(Collectors.toList());
    }

    private List<OrdersByWeekResponse> mapToOrdersByWeek(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new OrdersByWeekResponse(
                        row[0] != null ? row[0].toString() : "",
                        safeLong(row[1])
                ))
                .collect(Collectors.toList());
    }

    private List<OrdersByMonthResponse> mapToOrdersByMonth(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new OrdersByMonthResponse(
                        row[0] != null ? row[0].toString() : "",
                        safeLong(row[1])
                ))
                .collect(Collectors.toList());
    }

    private List<UserStatsByDayResponse> mapToUserStatsByDay(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new UserStatsByDayResponse(
                        (Date) row[0],
                        safeLong(row[1])
                ))
                .collect(Collectors.toList());
    }

    // ==================== HELPERS ====================
    private BigDecimal safeBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Long safeLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long) return (Long) val;
        try {
            return Long.valueOf(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // ==================== OTHER CHART API ====================
    public List<RevenueByDayResponse> getRevenueChart(DashboardRequest request) {
        return mapToRevenueByDay(orderRepository.getRevenueByDayRawWithDateRange(request.getFromDate(), request.getToDate()));
    }

    public List<OrdersByDayResponse> getOrdersChart(DashboardRequest request) {
        return mapToOrdersByDay(orderRepository.getOrdersByDayRawWithDateRange(request.getFromDate(), request.getToDate()));
    }

    public List<UserStatsByDayResponse> getUsersChart(DashboardRequest request) {
        return mapToUserStatsByDay(userRepository.getUserStatsByDayRawWithDateRange(request.getFromDate(), request.getToDate()));
    }

    public List<TopCategoryResponse> getTopCategories() {
        return productRepository.getTopCategories();
    }

    public List<TopProductResponse> getTopProducts() {
        List<TopProductResponse> topProducts = productRepository.getTopProducts();
        return topProducts.size() > 10 ? topProducts.subList(0, 10) : topProducts;
    }
}
