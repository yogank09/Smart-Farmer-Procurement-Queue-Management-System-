package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.LoginRequest;
import com.farmerprocurement.backend.dto.LoginResponse;
import com.farmerprocurement.backend.dto.RegisterRequest;
import com.farmerprocurement.backend.dto.RegisterResponse;
import com.farmerprocurement.backend.dto.VerifyEmailOtpRequest;
import com.farmerprocurement.backend.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


private final AuthService authService;

public AuthController(AuthService authService) {
    this.authService = authService;
}

// Register a new farmer.
@PostMapping("/register")
public ResponseEntity<RegisterResponse> register(
        @Valid @RequestBody RegisterRequest request) {

    RegisterResponse response = authService.register(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
}

// Verify the email OTP.
@PostMapping("/verify-email")
public ResponseEntity<Map<String, String>> verifyEmail(
        @Valid @RequestBody VerifyEmailOtpRequest request) {

    String message = authService.verifyEmailOtp(
            request.getEmail(),
            request.getOtp());

    return ResponseEntity.ok(
            Map.of("message", message));
}

// Log in and receive a JWT.
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request) {

    LoginResponse response = authService.login(request);

    return ResponseEntity.ok(response);
}


}
