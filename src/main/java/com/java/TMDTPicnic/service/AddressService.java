package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.AddressRequest;
import com.java.TMDTPicnic.dto.response.AddressResponse;
import com.java.TMDTPicnic.entity.Address;
import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.repository.AddressRepository;
import com.java.TMDTPicnic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // 🟢 Thêm địa chỉ
    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Nếu đánh dấu là địa chỉ mặc định → bỏ đánh dấu các địa chỉ khác
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.updateAllIsDefaultFalse(userId);
        }

        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .recipientName(request.getRecipientName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .detail(request.getDetail())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .createdAt(LocalDateTime.now())
                .build();

        addressRepository.save(address);
        return toResponse(address);
    }

    // 🟡 Cập nhật địa chỉ
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        Long userId = address.getUser().getId();

        // Nếu cập nhật thành mặc định → bỏ mặc định các địa chỉ khác
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.updateAllIsDefaultFalse(userId);
        }

        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setDetail(request.getDetail());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        addressRepository.save(address);
        return toResponse(address);
    }

    // 🔴 Xóa địa chỉ
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    // 🟣 Lấy danh sách địa chỉ của user
    public List<AddressResponse> getUserAddresses(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return addressRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 🧩 Hàm chuyển từ entity → response
    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getRecipientName(),
                address.getPhone(),
                address.getProvince(),
                address.getDistrict(),
                address.getWard(),
                address.getDetail(),
                address.getIsDefault(),
                address.getCreatedAt()
        );
    }
}
