package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.AddressRequest;
import com.java.TMDTPicnic.dto.response.AddressResponse;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    private boolean isAdmin(Jwt jwt) {
        return jwt != null && "ADMIN".equalsIgnoreCase(jwt.getClaimAsString("role"));
    }

    private Long getCurrentUserId(Jwt jwt) {
        // JWT claim “userId” chứa id người dùng khi đăng nhập
        Object idClaim = jwt.getClaim("userId");
        return idClaim != null ? Long.valueOf(idClaim.toString()) : null;
    }

    // === CREATE ADDRESS (USER chỉ được tạo cho chính mình) ===
    @PostMapping
    @Operation(summary = "Tạo địa chỉ mới cho người dùng")
    public ResponseEntity<ApiResponse<AddressResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId,
            @RequestBody AddressRequest request
    ) {
        Long currentUserId = getCurrentUserId(jwt);

        // Nếu không phải admin và userId khác user hiện tại -> cấm
        if (!isAdmin(jwt) && !userId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message("Bạn không có quyền tạo địa chỉ cho người khác")
                            .build());
        }

        AddressResponse response = addressService.createAddress(userId, request);
        return ResponseEntity.ok(
                ApiResponse.<AddressResponse>builder()
                        .message("Tạo địa chỉ thành công")
                        .data(response)
                        .build()
        );
    }

    // === UPDATE ADDRESS (ADMIN only) ===
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin địa chỉ (Admin)")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody AddressRequest request
    ) {
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message("Chỉ Admin mới được phép cập nhật địa chỉ")
                            .build());
        }

        AddressResponse response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(
                ApiResponse.<AddressResponse>builder()
                        .message("Cập nhật địa chỉ thành công")
                        .data(response)
                        .build()
        );
    }

    // === DELETE ADDRESS (ADMIN only) ===
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa địa chỉ theo ID (Admin)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>builder()
                            .message("Chỉ Admin mới được phép xóa địa chỉ")
                            .build());
        }

        addressService.deleteAddress(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Xóa địa chỉ thành công")
                        .build()
        );
    }

    // === GET USER ADDRESSES (USER chỉ xem của chính mình) ===
    @GetMapping
    @Operation(summary = "Lấy danh sách địa chỉ của người dùng")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long userId
    ) {
        Long currentUserId = getCurrentUserId(jwt);

        // Nếu không phải admin và userId khác người hiện tại -> cấm
        if (!isAdmin(jwt) && !userId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<AddressResponse>>builder()
                            .message("Bạn không có quyền xem địa chỉ của người khác")
                            .build());
        }

        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(
                ApiResponse.<List<AddressResponse>>builder()
                        .message("Lấy danh sách địa chỉ thành công")
                        .data(addresses)
                        .build()
        );
    }
}
