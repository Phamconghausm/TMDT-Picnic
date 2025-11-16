package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.ApplyCouponRequest;
import com.java.TMDTPicnic.dto.request.CouponCreateRequest;
import com.java.TMDTPicnic.dto.response.ApplyCouponResponse;
import com.java.TMDTPicnic.dto.response.CouponCreateResponse;
import com.java.TMDTPicnic.dto.response.CouponDTOResponse;
import com.java.TMDTPicnic.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * [Admin] Tạo mã giảm giá mới
     */
    @PostMapping("/create")
    public ResponseEntity<CouponCreateResponse> createCoupon(@RequestBody CouponCreateRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    /**
     * [User] Áp dụng mã giảm giá
     */
    @PostMapping("/apply")
    public ResponseEntity<ApplyCouponResponse> applyCoupon(@RequestBody ApplyCouponRequest request) {
        return ResponseEntity.ok(couponService.applyCoupon(request));
    }

    /**
     * [Admin/User] Xem thông tin mã giảm giá
     */
    @GetMapping("/{code}")
    public ResponseEntity<CouponDTOResponse> getCouponInfo(@PathVariable String code) {
        CouponDTOResponse dto = couponService.getCouponInfo(code);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
}
