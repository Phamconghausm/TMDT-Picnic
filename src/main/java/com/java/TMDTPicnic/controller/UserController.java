package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.ChangePasswordRequest;
import com.java.TMDTPicnic.dto.request.ForgotPasswordRequest;
import com.java.TMDTPicnic.dto.request.ResetPasswordRequest;
import com.java.TMDTPicnic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            Principal principal) {
        userService.changePassword(request, principal);
        return ResponseEntity.ok("Password changed successfully");
    }

}

