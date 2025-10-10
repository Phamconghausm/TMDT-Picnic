package com.java.TMDTPicnic.controller;
import com.java.TMDTPicnic.dto.request.AddToCartRequest;
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

    // ✅ Lấy giỏ hàng theo userId
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartByUser(userId));
    }

    // ✅ Thêm sản phẩm vào giỏ
    @PostMapping("/{userId}/add")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable Long userId,
            @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }

    // ✅ Xóa 1 sản phẩm khỏi giỏ
    @DeleteMapping("/{userId}/item/{productId}")
    public ResponseEntity<String> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.ok("Item removed successfully");
    }

    // ✅ Xóa toàn bộ giỏ hàng
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<String> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}

