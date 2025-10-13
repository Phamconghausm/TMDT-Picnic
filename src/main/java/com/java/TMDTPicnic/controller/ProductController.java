package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.ProductRequest;
import com.java.TMDTPicnic.dto.response.ProductResponse;
import com.java.TMDTPicnic.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // === CREATE PRODUCT (ADMIN only) ===
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ProductRequest request) {

        // Kiểm tra role từ JWT (nếu muốn)
        if (!jwt.getClaimAsString("role").equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ProductResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        ProductResponse createdProduct = productService.createProduct(request);
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Tạo sản phẩm thành công")
                        .data(createdProduct)
                        .build()
        );
    }
    // === GET ALL PRODUCTS (public) ===
    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm (có phân trang)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProductResponse> products = productService.getAllProducts(page, size);
        return ResponseEntity.ok(
                ApiResponse.<Page<ProductResponse>>builder()
                        .message("Lấy danh sách sản phẩm thành công")
                        .data(products)
                        .build()
        );
    }
    // === GET PRODUCT BY ID (public) ===
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Lấy sản phẩm thành công")
                        .data(product)
                        .build()
        );
    }
    // === UPDATE PRODUCT (ADMIN only) ===
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody ProductRequest request) {

        if (!jwt.getClaimAsString("role").equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ProductResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        ProductResponse updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Cập nhật sản phẩm thành công")
                        .data(updatedProduct)
                        .build()
        );
    }
    // === UPDATE PRODUCT (ADMIN only) ===
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody ProductRequest request
    ) {
        if (!jwt.getClaimAsString("role").equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Xoá sản phẩm thành công")
                        .build()
        );
    }
}
