package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.ProductRequest;
import com.java.TMDTPicnic.dto.response.ProductResponse;
import com.java.TMDTPicnic.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody ProductRequest request) {
        ProductResponse createdProduct = productService.createProduct(request);
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Tạo sản phẩm thành công")
                        .data(createdProduct)
                        .build()
        );
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Xoá sản phẩm thành công")
                        .build()
        );
    }
}
