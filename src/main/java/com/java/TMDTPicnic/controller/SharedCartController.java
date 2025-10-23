package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.SharedCartAddItemRequest;
import com.java.TMDTPicnic.dto.request.SharedCartCreateRequest;
import com.java.TMDTPicnic.dto.request.SharedCartInviteRequest;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.dto.response.SharedCartAddItemResponse;
import com.java.TMDTPicnic.dto.response.SharedCartCreateResponse;
import com.java.TMDTPicnic.dto.response.SharedCartInviteResponse;
import com.java.TMDTPicnic.service.SharedCartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shared-carts")
@RequiredArgsConstructor
public class SharedCartController {

    private final SharedCartService sharedCartService;

    // 1. Tạo giỏ hàng chia sẻ
    @Operation(summary = "Tạo giỏ hàng chia sẻ mới")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SharedCartCreateResponse>> createSharedCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SharedCartCreateRequest request) {

        Long ownerId = Long.valueOf(jwt.getClaimAsString("sub")); // Lấy userId từ token
        request.setOwnerId(ownerId); // Gán vào request

        SharedCartCreateResponse response = sharedCartService.createSharedCart(request);

        return ResponseEntity.ok(
                ApiResponse.<SharedCartCreateResponse>builder()
                        .message("Tạo giỏ hàng chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }

    // 2. Thêm sản phẩm vào giỏ
    @Operation(summary = "Thêm sản phẩm vào giỏ chia sẻ")
    @PostMapping("/add-item")
    public ResponseEntity<ApiResponse<SharedCartAddItemResponse>> addItemToCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SharedCartAddItemRequest request) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub")); // Lấy userId từ token
        request.setAddByUserId(userId);

        SharedCartAddItemResponse response = sharedCartService.addItemToCart(request);

        return ResponseEntity.ok(
                ApiResponse.<SharedCartAddItemResponse>builder()
                        .message("Thêm sản phẩm vào giỏ chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }

    // 3. Mời người dùng tham gia giỏ hàng
    @Operation(summary = "Mời người tham gia giỏ chia sẻ (bằng email hoặc username)")
    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<List<SharedCartInviteResponse>>> inviteParticipants(
            @RequestBody SharedCartInviteRequest request) {
        List<SharedCartInviteResponse> responses = sharedCartService.addParticipant(request);
        return ResponseEntity.ok(
                ApiResponse.<List<SharedCartInviteResponse>>builder()
                        .message("Mời người tham gia giỏ chia sẻ thành công")
                        .data(responses)
                        .build()
        );
    }

    // 4. Lấy chi tiết giỏ chia sẻ
    @Operation(summary = "Lấy chi tiết giỏ hàng chia sẻ (bao gồm sản phẩm và người tham gia)")
    @GetMapping("/{cartId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCartDetails(
            @PathVariable Long cartId) {
        Map<String, Object> response = sharedCartService.getCartDetails(cartId);
        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .message("Lấy chi tiết giỏ hàng chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }
}
