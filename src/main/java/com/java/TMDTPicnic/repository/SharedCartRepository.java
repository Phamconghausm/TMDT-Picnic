package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.SharedCart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedCartRepository extends JpaRepository<SharedCart, Long> {
}
