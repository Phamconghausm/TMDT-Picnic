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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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

//        productRepository.save(product);

        // Upload tất cả ảnh
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                ProductImage image = uploadImageToCloudinary(file, product);
                image.setProduct(product); // Quan trọng
                product.getImages().add(image); // Lưu vào danh sách ảnh của product
            }
        }

        productRepository.save(product);
        return mapToResponse(product);
    }

    // Cập nhật sản phẩm kèm upload thêm nhiều ảnh
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

        // Upload thêm ảnh mới
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                ProductImage image = uploadImageToCloudinary(file, product);
                product.getImages().add(image);
            }
        }

        productRepository.save(product);
        return mapToResponse(product);
    }


    public Page<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());


//        return productRepository.findAll()
//                .stream().map(this::mapToResponse)
//                .collect(Collectors.toList());
        Page<Product> productsPage = productRepository.findAll(pageable);

        return productsPage.map(this::mapToResponse);
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
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
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

    // Hàm để upload ảnh lên Cloudinary
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
