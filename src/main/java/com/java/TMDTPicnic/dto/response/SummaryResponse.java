package com.java.TMDTPicnic.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SummaryResponse {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private OrderStatusResponse ordersStatus;
    private Long totalProducts;
    private Long productsSold;
    private Long totalUsers;
    private Long totalUsersActive;
    // New KPI metrics
    private List<TopProductResponse> topSellingProducts; // Top 5-10 products by quantity sold
    private Long openSharedCarts; // SharedCart status = OPEN
    private Long activeGroupBuyCampaigns; // GroupBuyCampaign status = ACTIVE
    private Long activeCoupons; // Coupon validFrom <= now <= validTo
}
