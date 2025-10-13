package com.java.TMDTPicnic.dto.request;

import com.java.TMDTPicnic.entity.Product;
import com.java.TMDTPicnic.entity.SharedCart;
import jakarta.persistence.*;

import java.math.BigDecimal;

public class SharedCartItemRequest {
    private SharedCart sharedCart;
    private Product product;
    private Integer quantity;
    private BigDecimal priceAtAdd;
}
