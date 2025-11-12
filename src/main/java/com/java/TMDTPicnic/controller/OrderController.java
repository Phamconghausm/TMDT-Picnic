package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.CheckoutRequest;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.service.OrderService;
import com.java.TMDTPicnic.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final VNPayService vnPayService;

    /**
     * Đặt hàng (checkout) - thanh toán bằng VNPay
     * Hỗ trợ đặt hàng từ:
     * - Giỏ hàng (orderType = "GROUP", dùng cartItems)
     * - Đặt trực tiếp (orderType = "SINGLE", dùng directItems)
     *
     * @param jwt: lấy userId từ token
     * @param request: dữ liệu đặt hàng
     * @param httpRequest: lấy IP client
     * @return URL thanh toán VNPay
     */
    @PostMapping("/checkout-vnpay")
    public ResponseEntity<ApiResponse<String>> checkoutVNPay(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CheckoutRequest request,
            HttpServletRequest httpRequest) throws UnsupportedEncodingException {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));

        // Lấy IP client chính xác, hỗ trợ proxy
        String ipAddress = extractClientIp(httpRequest);

        // Gọi service tạo đơn và lấy URL thanh toán
        String paymentUrl = orderService.createOrder(userId, request, ipAddress);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Tạo đơn hàng thành công. Chuyển hướng đến VNPay để thanh toán.")
                        .data(paymentUrl)
                        .build()
        );
    }

    /**
     * Callback khi người dùng thanh toán xong và VNPay redirect về returnUrl
     * VD: /api/orders/vnpay-return?vnp_Amount=...&vnp_ResponseCode=00&vnp_SecureHash=...
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<String>> handleVNPayReturn(@RequestParam Map<String, String> vnpParams) {
        boolean valid = vnPayService.validateReturn(vnpParams);
        String responseCode = vnpParams.get("vnp_ResponseCode");
        Long orderId = Long.valueOf(vnpParams.get("vnp_TxnRef"));

        if (!valid) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                            .message("Xác minh chữ ký thất bại.")
                            .data(null)
                            .build()
            );
        }

        if ("00".equals(responseCode)) {
            // Thanh toán thành công, cập nhật trạng thái
            orderService.updatePaymentStatusAfterSuccess(orderId);

            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Thanh toán thành công cho đơn hàng #" + orderId)
                            .data("success")
                            .build()
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Thanh toán thất bại hoặc bị hủy cho đơn hàng #" + orderId)
                            .data("failed")
                            .build()
            );
        }
    }




    /**
     * Lấy IP client hỗ trợ trường hợp qua proxy
     */
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Trường hợp IP có nhiều IP (do proxy chain), lấy IP đầu tiên
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
