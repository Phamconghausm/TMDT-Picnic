package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.SharedCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedCartItemRepository extends JpaRepository<SharedCartItem, Long> {
    List<SharedCartItem> findBySharedCartId(Long sharedCartId);
}
