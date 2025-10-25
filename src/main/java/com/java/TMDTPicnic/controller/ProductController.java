package com.java.TMDTPicnic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.TMDTPicnic.dto.request.ProductRequest;
import com.java.TMDTPicnic.dto.response.ProductResponse;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper mapper = new ObjectMapper();

    // === TẠO SẢN PHẨM (ADMIN) ===
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {

        if (!"ROLE_ADMIN".equals(jwt.getClaimAsString("scope"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ProductResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        ProductRequest request = mapper.readValue(productJson, ProductRequest.class);
        ProductResponse createdProduct = productService.createProduct(request, images);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Tạo sản phẩm thành công")
                        .data(createdProduct)
                        .build()
        );
    }

    // === LẤY DANH SÁCH SẢN PHẨM (KHÔNG PHÂN TRANG) ===
    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả sản phẩm (không phân trang)")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // === LỌC SẢN PHẨM THEO LOẠI (mới nhất, giảm giá, bán chạy) ===
    @GetMapping("/filter")
    @Operation(summary = "Lọc sản phẩm theo loại (newest, discount, best-seller)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductsByFilter(
            @RequestParam String filter) {

        Map<String, Object> data = productService.getProductsByFilter(filter);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .message("Lấy sản phẩm theo filter thành công")
                        .data(data)
                        .build()
        );
    }


    // === LẤY CHI TIẾT SẢN PHẨM THEO ID ===
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết sản phẩm theo ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Lấy sản phẩm thành công")
                        .data(product)
                        .build()
        );
    }

    // === CẬP NHẬT SẢN PHẨM (ADMIN) ===
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {

        if (!"ROLE_ADMIN".equals(jwt.getClaimAsString("scope"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ProductResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        ProductRequest request = mapper.readValue(productJson, ProductRequest.class);
        ProductResponse updatedProduct = productService.updateProduct(id, request, images);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Cập nhật sản phẩm thành công")
                        .data(updatedProduct)
                        .build()
        );
    }

    // === XÓA SẢN PHẨM (ADMIN) ===
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm theo ID")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        if (!"ROLE_ADMIN".equals(jwt.getClaimAsString("scope"))) {
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
