package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.AddressRequest;
import com.java.TMDTPicnic.dto.response.AddressResponse;
import com.java.TMDTPicnic.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> create(@RequestParam Long userId, @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.createAddress(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(@PathVariable Long id, @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@RequestParam Long userId) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }
}
