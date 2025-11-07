package com.java.TMDTPicnic.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.java.TMDTPicnic.dto.request.ProductRequest;
import com.java.TMDTPicnic.dto.response.ProductImageResponse;
import com.java.TMDTPicnic.dto.response.ProductResponse;
import com.java.TMDTPicnic.entity.Category;
import com.java.TMDTPicnic.entity.Product;
import com.java.TMDTPicnic.entity.ProductImage;
import com.java.TMDTPicnic.repository.CategoryRepository;
import com.java.TMDTPicnic.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Cloudinary cloudinary;

    // Tạo sản phẩm kèm upload nhiều ảnh
    public ProductResponse createProduct(ProductRequest request, List<MultipartFile> imageFiles) {
        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setUnit(request.getUnit());
        product.setIsActive(request.getIsActive());
        product.setCreatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                ProductImage image = uploadImageToCloudinary(file, product);
                image.setProduct(product);
                product.getImages().add(image);
            }
        }

        productRepository.save(product);
        return mapToResponse(product);
    }
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));
        return mapToResponse(product);
    }

    // Cập nhật sản phẩm
    public ProductResponse updateProduct(Long id, ProductRequest request, List<MultipartFile> imageFiles) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setUnit(request.getUnit());
        product.setIsActive(request.getIsActive());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                ProductImage image = uploadImageToCloudinary(file, product);
                product.getImages().add(image);
            }
        }

        productRepository.save(product);
        return mapToResponse(product);
    }
    // Lấy danh sách sản phẩm theo categoryId
    public List<ProductResponse> getProductsByCategoryId(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        if (products.isEmpty()) {
            throw new RuntimeException("No products found for category ID: " + categoryId);
        }
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Lấy toàn bộ sản phẩm
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToResponse).toList();
    }

    // Lọc sản phẩm theo tiêu chí, không phân theo category
    public List<ProductResponse> getProductsByFilter(String filter) {
        switch (filter.toLowerCase()) {

            // === FEATURED ===
            case "featured" -> {
                return productRepository.findByIsFeaturedTrue()
                        .stream().limit(10)
                        .map(this::mapToResponse)
                        .toList();
            }

            // === NEWEST ===
            case "newest" -> {
                return productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                        .stream().limit(10)
                        .map(this::mapToResponse)
                        .toList();
            }

            // === DISCOUNT ===
            case "discount" -> {
                return productRepository.findByDiscountRateGreaterThan(BigDecimal.ZERO)
                        .stream()
                        .sorted((a, b) -> b.getDiscountRate().compareTo(a.getDiscountRate()))
                        .limit(10)
                        .map(this::mapToResponse)
                        .toList();
            }

            // === BEST SELLER ===
            case "best-seller" -> {
                return productRepository.findAll(Sort.by(Sort.Direction.DESC, "soldQuantity"))
                        .stream().limit(10)
                        .map(this::mapToResponse)
                        .toList();
            }

            default -> throw new IllegalArgumentException("Invalid filter type: " + filter);
        }
    }




    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    // Chuyển đổi entity sang DTO
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .discountRate(product.getDiscountRate())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .unit(product.getUnit())
                .soldQuantity(product.getSoldQuantity())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .isFeatured(product.getIsFeatured())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .images(product.getImages() != null ? product.getImages().stream()
                        .map(img -> ProductImageResponse.builder()
                                .id(img.getId())
                                .url(img.getUrl())
                                .altText(img.getAltText())
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }

    // Upload ảnh lên Cloudinary
    private ProductImage uploadImageToCloudinary(MultipartFile file, Product product) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "products"));

            ProductImage image = new ProductImage();
            image.setUrl(uploadResult.get("secure_url").toString());
            image.setAltText(product.getName());
            image.setProduct(product);
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }
}
