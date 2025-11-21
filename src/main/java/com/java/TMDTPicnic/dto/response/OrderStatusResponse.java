package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class OrderStatusResponse {
    private Long completedOrders;
    private Long pendingOrders;
    private Long paidOrders;


    public OrderStatusResponse(Number completedOrders, Number processingOrders, Number paidOrders) {
        this.completedOrders = completedOrders != null ? completedOrders.longValue() : 0L;
        this.pendingOrders = processingOrders != null ? processingOrders.longValue() : 0L;
        this.paidOrders = paidOrders != null ? paidOrders.longValue() : 0L;
    }
}
