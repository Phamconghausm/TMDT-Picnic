package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Builder
public class ApplyCouponRequest {
    private String code;           // mã người dùng nhập
    private BigDecimal orderTotal; // tổng giá trị đơn hàng trước khi giảm
}
