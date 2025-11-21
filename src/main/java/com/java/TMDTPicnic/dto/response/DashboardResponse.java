package com.java.TMDTPicnic.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResponse {

    private SummaryResponse summary;

//    // Revenue charts
//    private List<RevenueByDayResponse> revenueByDay;
//    private List<RevenueByWeekResponse> revenueByWeek;
//    private List<RevenueByMonthResponse> revenueByMonth;
//
//    // Order charts
//    private List<OrdersByDayResponse> ordersByDay;
//    private List<OrdersByWeekResponse> ordersByWeek;
//    private List<OrdersByMonthResponse> ordersByMonth;
//
//    // User stats
//    private List<UserStatsByDayResponse> userStatsByDay;
//
//    // Top items
//    private List<TopCategoryResponse> topCategories;
//    private List<TopProductResponse> topProducts;
//
//    private List<TopCategoryResponse> topCategoriesTop3;
//    private List<TopProductResponse> topProductsTop3;
//
//    private OrderStatusResponse orderStatus;
//
//    // New sections
//    private List<OrderTypeDistributionResponse> orderTypeDistribution; // SINGLE vs GROUP
//    private List<OrderStatusDistributionResponse> orderStatusDistribution; // For pie chart
//
//    // User section
//    private List<UserRoleDistributionResponse> userRoleDistribution;
//    private List<TopActiveUserResponse> topActiveUsers;
//
//    // Product section
//    private ProductStatsResponse productStats;
//
//    // Shared Cart & Group Buy section
//    private SharedCartStatsResponse sharedCartStats;
//    private GroupBuyStatsResponse groupBuyStats;
//
//    // Coupon section
//    private CouponStatsResponse couponStats;
//
//    // Review section
//    private ReviewStatsResponse reviewStats;
//
//    // Alerts
//    private DashboardAlertsResponse alerts;
}
