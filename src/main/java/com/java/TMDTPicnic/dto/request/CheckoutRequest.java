package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckoutRequest {
    private List<CheckoutItemRequest> items;
    private String orderType; // SINGLE / GROUP
    private String paymentMethod; // COD, VNPAY, MOMO
}
