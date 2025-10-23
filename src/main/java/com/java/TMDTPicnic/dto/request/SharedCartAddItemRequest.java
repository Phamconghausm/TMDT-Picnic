package com.java.TMDTPicnic.dto.request;

import com.java.TMDTPicnic.entity.Product;
import com.java.TMDTPicnic.entity.SharedCart;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SharedCartAddItemRequest {
    private Long sharedCartId;
    private Long productId;
    private Long addByUserId;
    private Integer quantity;
    private BigDecimal priceAtAdd;
}