package com.java.TMDTPicnic.controller;
import com.java.TMDTPicnic.dto.request.AddToCartRequest;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.dto.response.CartResponse;
import com.java.TMDTPicnic.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        CartResponse cart = cartService.getCartByUser(userId);
        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .message("Lấy giỏ hàng thành công")
                        .data(cart)
                        .build()
        );
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @PathVariable Long userId,
            @RequestBody AddToCartRequest request) {

        CartResponse updatedCart = cartService.addToCart(userId, request);

        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .message("Thêm sản phẩm vào giỏ thành công")
                        .data(updatedCart)
                        .build()
        );
    }

    @DeleteMapping("/{userId}/item/{productId}")
    public ResponseEntity<ApiResponse<String>> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId) {

        cartService.removeItem(userId, productId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                        .data("Item removed")
                        .build()
        );
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xóa toàn bộ giỏ hàng thành công")
                        .data("Cart cleared")
                        .build()
        );
    }
}