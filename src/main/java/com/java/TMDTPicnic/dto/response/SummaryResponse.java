package com.java.TMDTPicnic.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SummaryResponse {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private OrderStatusResponse ordersCompleted;
    private Long totalProducts;
    private Long productsSold;
//    private Double totalProfit;
    private Long totalUsers;
    private Long newUsers;
}
