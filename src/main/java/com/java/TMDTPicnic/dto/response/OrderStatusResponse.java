package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusResponse {
    private Long newOrders;
    private Long processingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
}
