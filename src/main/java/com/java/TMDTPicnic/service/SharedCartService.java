package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.SharedCartAddItemRequest;
import com.java.TMDTPicnic.dto.request.SharedCartCreateRequest;
import com.java.TMDTPicnic.dto.request.SharedCartInviteRequest;
import com.java.TMDTPicnic.dto.response.SharedCartAddItemResponse;
import com.java.TMDTPicnic.dto.response.SharedCartCreateResponse;
import com.java.TMDTPicnic.dto.response.SharedCartInviteResponse;
import com.java.TMDTPicnic.entity.*;
import com.java.TMDTPicnic.enums.SharedCartStatus;
import com.java.TMDTPicnic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SharedCartService {

    private final SharedCartRepository sharedCartRepository;
    private final SharedCartItemRepository sharedCartItemRepository;
    private final SharedCartParticipantRepository sharedCartParticipantRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // 1. Tạo giỏ chia sẻ
    public SharedCartCreateResponse createSharedCart(SharedCartCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        SharedCart cart = SharedCart.builder()
                .title(request.getTitle())
                .owner(owner)
                .status(SharedCartStatus.OPEN)
                .expiresAt(request.getExpiresAt())
                .createdAt(LocalDateTime.now())
                .build();

        sharedCartRepository.save(cart);

        return toCartResponse(cart);
    }

    // 2. Thêm sản phẩm vào giỏ
    public SharedCartAddItemResponse addItemToCart(SharedCartAddItemRequest request) {
        SharedCart cart = sharedCartRepository.findById(request.getSharedCartId())
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        User user = userRepository.findById(request.getAddByUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        SharedCartItem item = SharedCartItem.builder()
                .sharedCart(cart)
                .product(product)
                .user(user)
                .quantity(request.getQuantity())
                .priceAtAdd(request.getPriceAtAdd())
                .build();

        sharedCartItemRepository.save(item);
        return toItemResponse(item);
    }

    // 3. Mời người tham gia giỏ bằng email hoặc username
    public List<SharedCartInviteResponse> addParticipant(SharedCartInviteRequest request) {
        SharedCart cart = sharedCartRepository.findById(request.getSharedCartId())
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        List<SharedCartParticipant> participants = new ArrayList<>();

        // Lặp qua từng username/email để tìm user tương ứng
        for (String identifier : request.getIdentifiers()) {
            Optional<User> userOpt = userRepository.findByEmail(identifier);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(identifier);
            }

            User user = userOpt.orElseThrow(() ->
                    new RuntimeException("User not found with username/email: " + identifier));

            // Kiểm tra trùng lặp
            boolean exists = sharedCartParticipantRepository.existsBySharedCartAndUser(cart, user);
            if (exists) continue;

            SharedCartParticipant participant = SharedCartParticipant.builder()
                    .sharedCart(cart)
                    .user(user)
                    .contributionAmount(request.getContributionAmount())
                    .joinedAt(LocalDateTime.now())
                    .build();

            participants.add(participant);
        }

        if (participants.isEmpty()) {
            throw new RuntimeException("No valid users to invite");
        }

        sharedCartParticipantRepository.saveAll(participants);
        return participants.stream()
                .map(this::toParticipantResponse)
                .collect(Collectors.toList());
    }

    // 4. Lấy chi tiết giỏ
    public Map<String, Object> getCartDetails(Long cartId) {
        SharedCart cart = sharedCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        List<SharedCartItem> items = sharedCartItemRepository.findBySharedCartId(cartId);
        List<SharedCartParticipant> participants = sharedCartParticipantRepository.findBySharedCartId(cartId);

        Map<String, Object> response = new HashMap<>();
        response.put("cart", toCartResponse(cart));
        response.put("items", items.stream().map(this::toItemResponse).toList());
        response.put("participants", participants.stream().map(this::toParticipantResponse).toList());

        return response;
    }

    // Convert methods
    private SharedCartCreateResponse toCartResponse(SharedCart cart) {
        return SharedCartCreateResponse.builder()
                .id(cart.getId())
                .title(cart.getTitle())
                .ownerId(cart.getOwner().getId())
                .expiresAt(cart.getExpiresAt())
                .status(cart.getStatus())
                .createdAt(cart.getCreatedAt())
                .build();
    }

    private SharedCartAddItemResponse toItemResponse(SharedCartItem item) {
        return SharedCartAddItemResponse.builder()
                .id(item.getId())
                .sharedCartId(item.getSharedCart().getId())
                .productId(item.getProduct().getId())
                .userId(item.getUser().getId())
                .quantity(item.getQuantity())
                .priceAtAdd(item.getPriceAtAdd())
                .build();
    }

    private SharedCartInviteResponse toParticipantResponse(SharedCartParticipant p) {
        return SharedCartInviteResponse.builder()
                .id(p.getId())
                .sharedCartId(p.getSharedCart().getId())
                .userId(p.getUser().getId())
                .contributionAmount(p.getContributionAmount())
                .joinedAt(p.getJoinedAt())
                .build();
    }
}
