package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
