package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.DashboardRequest;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SharedCartRepository sharedCartRepository;
    private final GroupBuyCampaignRepository groupBuyCampaignRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    // ==================== DASHBOARD SUMMARY ONLY ====================
    public DashboardResponse getDashboard(DashboardRequest request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();

        logger.info("DashboardService.getDashboard called with fromDate={}, toDate={}", fromDate, toDate);

        DashboardResponse response = new DashboardResponse();

        try {
            logger.info("Building summary...");
            response.setSummary(buildSummary(fromDate, toDate));
            logger.info("Summary built: {}", response.getSummary());

            // Bỏ các phần chi tiết theo ngày/tuần/tháng đi

            logger.info("DashboardResponse with summary built successfully");
            return response;
        } catch (Exception e) {
            logger.error("Error building dashboard response", e);
            throw e;
        }
    }

    // ==================== SUMMARY BUILDER ====================
    private SummaryResponse buildSummary(LocalDate fromDate, LocalDate toDate) {
        SummaryResponse summary = new SummaryResponse();

        summary.setTotalRevenue(
                safeBigDecimal(orderRepository.getTotalRevenueWithDateRange(fromDate, toDate)));
        summary.setTotalOrders(orderRepository.getTotalOrdersWithDateRange(fromDate, toDate));
        summary.setTotalUsers(userRepository.getTotalUsersWithDateRange(fromDate, toDate));
        summary.setTotalUsersActive(userRepository.getTotalUsersActiveWithDateRange(fromDate, toDate));

        // Các chỉ số tổng hợp khác
        summary.setOrdersStatus(orderRepository.getOrderStatusWithDayRange(fromDate, toDate));
        summary.setTotalProducts(productRepository.countTotalProducts());
        summary.setProductsSold(safeLong(orderItemRepository.countTotalSoldWithDateRange(fromDate, toDate)));

        try {
            Pageable top10 = PageRequest.of(0, 10);
            List<TopProductResponse> topProducts = productRepository.getTopProducts(top10);
            summary.setTopSellingProducts(topProducts);

            summary.setOpenSharedCarts(sharedCartRepository.countOpenSharedCarts());
            summary.setActiveGroupBuyCampaigns(groupBuyCampaignRepository.countActiveCampaigns());
            summary.setActiveCoupons(couponRepository.countActiveCoupons());
        } catch (Exception e) {
            logger.warn("Error loading KPI metrics, setting default values", e);
            summary.setTopSellingProducts(null);
            summary.setOpenSharedCarts(0L);
            summary.setActiveGroupBuyCampaigns(0L);
            summary.setActiveCoupons(0L);
        }

        return summary;
    }
//    // ==================== TOP ITEMS SETTER ====================
//    private void setTopCategories(DashboardResponse response) {
//        List<TopCategoryResponse> categories = productRepository.getTopCategories();
//        response.setTopCategories(categories);
//        response.setTopCategoriesTop3(categories.stream().limit(3).collect(Collectors.toList()));
//    }
//
//    private void setTopProducts(DashboardResponse response) {
//        List<TopProductResponse> products = productRepository.getTopProducts();
//        response.setTopProducts(products.size() > 10 ? products.subList(0, 10) : products);
//        response.setTopProductsTop3(products.stream().limit(3).collect(Collectors.toList()));
//    }

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
        Pageable top10 = PageRequest.of(0, 10);
        return productRepository.getTopCategories(top10);
    }

    public List<TopProductResponse> getTopProducts() {
        Pageable top10 = PageRequest.of(0, 10);
        List<TopProductResponse> topProducts = productRepository.getTopProducts(top10);
        return topProducts.size() > 10 ? topProducts.subList(0, 10) : topProducts;
    }

    // ==================== NEW SECTION BUILDERS ====================
    private List<TopActiveUserResponse> mapToTopActiveUsers(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new TopActiveUserResponse(
                        safeLong(row[0]), // userId
                        row[1] != null ? row[1].toString() : "", // username
                        row[2] != null ? row[2].toString() : "", // email
                        safeLong(row[3]), // orderCount
                        safeLong(row[4])  // reviewCount
                ))
                .collect(Collectors.toList());
    }

    private ProductStatsResponse buildProductStats() {
        try {
            Long totalActive = productRepository.countActiveProducts();
            Long totalInactive = productRepository.countInactiveProducts();
            Long lowStockCount = productRepository.countLowStockProducts(10); // threshold = 10

            List<TopProductResponse> topRatedProducts = mapToTopProductsFromRaw(reviewRepository.getTopRatedProductsRaw());
            List<TopProductResponse> lowRatedProducts = mapToTopProductsFromRaw(reviewRepository.getLowRatedProductsRaw());

            // Count high/low rated products
            Long highRatedCount = (long) topRatedProducts.size();
            Long lowRatedCount = (long) lowRatedProducts.size();

            return new ProductStatsResponse(
                    totalActive != null ? totalActive : 0L,
                    totalInactive != null ? totalInactive : 0L,
                    lowStockCount != null ? lowStockCount : 0L,
                    highRatedCount,
                    lowRatedCount,
                    topRatedProducts,
                    lowRatedProducts
            );
        } catch (Exception e) {
            logger.warn("Error building product stats", e);
            return new ProductStatsResponse(0L, 0L, 0L, 0L, 0L, new ArrayList<>(), new ArrayList<>());
        }
    }

    private List<TopProductResponse> mapToTopProductsFromRaw(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new TopProductResponse(
                        safeLong(row[0]), // productId
                        row[1] != null ? row[1].toString() : "", // productName
                        safeInteger(row[2]), // unitsSold (Integer)
                        safeBigDecimal(row[3]) // revenue
                ))
                .collect(Collectors.toList());
    }

    private SharedCartStatsResponse buildSharedCartStats() {
        try {
            Long openCarts = sharedCartRepository.countOpenSharedCarts();
            Long closedCarts = sharedCartRepository.countClosedSharedCarts();
            Long expiringSoon = sharedCartRepository.countExpiringSoonSharedCarts();

            List<TopProductResponse> topProducts = mapToTopProductsFromRaw(
                    productRepository.getTopProductsInSharedCartsRaw()
            );

            return new SharedCartStatsResponse(
                    openCarts != null ? openCarts : 0L,
                    closedCarts != null ? closedCarts : 0L,
                    expiringSoon != null ? expiringSoon : 0L,
                    topProducts
            );
        } catch (Exception e) {
            logger.warn("Error building shared cart stats", e);
            return new SharedCartStatsResponse(0L, 0L, 0L, new ArrayList<>());
        }
    }

    private GroupBuyStatsResponse buildGroupBuyStats() {
        try {
            Long activeCampaigns = groupBuyCampaignRepository.countActiveCampaigns();
            Long endingSoon = groupBuyCampaignRepository.countEndingSoonCampaigns();
            Long successful = groupBuyCampaignRepository.countSuccessfulCampaigns();
            Long failed = groupBuyCampaignRepository.countFailedCampaigns();

            return new GroupBuyStatsResponse(
                    activeCampaigns != null ? activeCampaigns : 0L,
                    endingSoon != null ? endingSoon : 0L,
                    successful != null ? successful : 0L,
                    failed != null ? failed : 0L
            );
        } catch (Exception e) {
            logger.warn("Error building group buy stats", e);
            return new GroupBuyStatsResponse(0L, 0L, 0L, 0L);
        }
    }

    private CouponStatsResponse buildCouponStats() {
        try {
            Long activeCoupons = couponRepository.countActiveCoupons();
            Long expiredCoupons = couponRepository.countExpiredCoupons();
            Long fullyUsedCoupons = couponRepository.countFullyUsedCoupons();
            Double usageRate = couponRepository.getCouponUsageRate();

            List<TopCouponResponse> topUsedCoupons = mapToTopCoupons(couponRepository.getTopUsedCouponsRaw());

            return new CouponStatsResponse(
                    activeCoupons != null ? activeCoupons : 0L,
                    expiredCoupons != null ? expiredCoupons : 0L,
                    fullyUsedCoupons != null ? fullyUsedCoupons : 0L,
                    usageRate != null ? usageRate : 0.0,
                    topUsedCoupons
            );
        } catch (Exception e) {
            logger.warn("Error building coupon stats", e);
            return new CouponStatsResponse(0L, 0L, 0L, 0.0, new ArrayList<>());
        }
    }

    private List<TopCouponResponse> mapToTopCoupons(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new TopCouponResponse(
                        safeLong(row[0]), // couponId
                        row[1] != null ? row[1].toString() : "", // code
                        safeInteger(row[2]), // usedCount
                        safeInteger(row[3])  // usageLimit
                ))
                .collect(Collectors.toList());
    }

    private ReviewStatsResponse buildReviewStats() {
        try {
            Long newReviewsThisWeek = reviewRepository.countNewReviewsThisWeek();
            Long newReviewsThisMonth = reviewRepository.countNewReviewsThisMonth();
            Long hiddenReviews = reviewRepository.countHiddenReviews();

            List<TopProductResponse> topRatedProducts = mapToTopProductsFromRaw(
                    reviewRepository.getTopRatedProductsRaw()
            );
            List<TopProductResponse> lowRatedProducts = mapToTopProductsFromRaw(
                    reviewRepository.getLowRatedProductsRaw()
            );

            return new ReviewStatsResponse(
                    newReviewsThisWeek != null ? newReviewsThisWeek : 0L,
                    newReviewsThisMonth != null ? newReviewsThisMonth : 0L,
                    hiddenReviews != null ? hiddenReviews : 0L,
                    topRatedProducts,
                    lowRatedProducts
            );
        } catch (Exception e) {
            logger.warn("Error building review stats", e);
            return new ReviewStatsResponse(0L, 0L, 0L, new ArrayList<>(), new ArrayList<>());
        }
    }

    private DashboardAlertsResponse buildAlerts() {
        try {
            Long overdueOrders = orderRepository.countOverdueOrders();
            Long lowStockProducts = productRepository.countLowStockProducts(10);
            Long endingGroupBuyCampaigns = groupBuyCampaignRepository.countEndingSoonCampaigns();
            Long expiringSharedCarts = sharedCartRepository.countExpiringSoonSharedCarts();

            List<DashboardAlertsResponse.AlertDetail> alertDetails = new ArrayList<>();
            if (overdueOrders > 0) {
                alertDetails.add(new DashboardAlertsResponse.AlertDetail("ORDER_OVERDUE", 
                    "Có " + overdueOrders + " đơn hàng quá hạn chưa xử lý", overdueOrders));
            }
            if (lowStockProducts > 0) {
                alertDetails.add(new DashboardAlertsResponse.AlertDetail("LOW_STOCK", 
                    "Có " + lowStockProducts + " sản phẩm gần hết hàng", lowStockProducts));
            }
            if (endingGroupBuyCampaigns > 0) {
                alertDetails.add(new DashboardAlertsResponse.AlertDetail("GROUP_BUY_ENDING", 
                    "Có " + endingGroupBuyCampaigns + " chiến dịch mua chung sắp kết thúc", endingGroupBuyCampaigns));
            }
            if (expiringSharedCarts > 0) {
                alertDetails.add(new DashboardAlertsResponse.AlertDetail("SHARED_CART_EXPIRING", 
                    "Có " + expiringSharedCarts + " giỏ hàng chia sẻ sắp hết hạn", expiringSharedCarts));
            }

            return new DashboardAlertsResponse(
                    overdueOrders != null ? overdueOrders : 0L,
                    lowStockProducts != null ? lowStockProducts : 0L,
                    endingGroupBuyCampaigns != null ? endingGroupBuyCampaigns : 0L,
                    expiringSharedCarts != null ? expiringSharedCarts : 0L,
                    alertDetails
            );
        } catch (Exception e) {
            logger.warn("Error building alerts", e);
            return new DashboardAlertsResponse(0L, 0L, 0L, 0L, new ArrayList<>());
        }
    }

    private Integer safeInteger(Object val) {
        if (val == null) return 0;
        if (val instanceof Integer) return (Integer) val;
        try {
            return Integer.valueOf(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
