package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySlug(String slug);
    Optional<Product> findBySlug(String slug);
    List<Product> findByIsFeaturedTrue();
    List<Product> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);
    List<Product> findByDiscountRateGreaterThan(BigDecimal rate);
    List<Product> findByCategoryIdOrderBySoldQuantityDesc(Long categoryId);
    List<Product> findByCategoryIdAndDiscountRateGreaterThanOrderByDiscountRateDesc(Long categoryId, BigDecimal discountRate);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
}
