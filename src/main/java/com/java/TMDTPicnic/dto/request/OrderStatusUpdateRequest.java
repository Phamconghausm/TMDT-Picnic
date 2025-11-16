package com.java.TMDTPicnic.dto.request;

import com.java.TMDTPicnic.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusUpdateRequest {
    private OrderStatus status;
}
