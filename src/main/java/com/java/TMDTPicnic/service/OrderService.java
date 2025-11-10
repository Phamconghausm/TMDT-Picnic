package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
//    private final OrderRepository orderRepository;
//    private final OrderItemRepository orderItemRepository;
//    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
}
