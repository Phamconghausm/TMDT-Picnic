package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySlug(String slug);
    List<Product> findByIsFeaturedTrue();
    List<Product> findByDiscountRateGreaterThan(Integer discountRate);
}
