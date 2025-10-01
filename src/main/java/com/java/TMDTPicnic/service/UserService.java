package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.ChangePasswordRequest;
import com.java.TMDTPicnic.exception.AppException;
import com.java.TMDTPicnic.exception.ErrorCode;
import com.java.TMDTPicnic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(ChangePasswordRequest request, Principal principal) {
        // Lấy user hiện tại dựa trên email trong token
        var user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        // Update mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }


}
