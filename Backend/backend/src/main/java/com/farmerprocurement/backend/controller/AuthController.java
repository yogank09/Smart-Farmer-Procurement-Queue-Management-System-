package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.LoginRequest;
import com.farmerprocurement.backend.dto.LoginResponse;
import com.farmerprocurement.backend.dto.AuthSessionResponse;
import com.farmerprocurement.backend.dto.RegisterRequest;
import com.farmerprocurement.backend.dto.RegisterResponse;
import com.farmerprocurement.backend.dto.VerifyEmailOtpRequest;
import com.farmerprocurement.backend.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.farmerprocurement.backend.security.JwtService;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


private final AuthService authService;
private final JwtService jwtService;
private final boolean secureCookie;
private final String cookieSameSite;

public AuthController(
        AuthService authService,
        JwtService jwtService,
        @org.springframework.beans.factory.annotation.Value("${app.auth-cookie-secure:false}") boolean secureCookie,
        @org.springframework.beans.factory.annotation.Value("${app.auth-cookie-same-site:Strict}") String cookieSameSite) {
    this.authService = authService;
    this.jwtService = jwtService;
    this.secureCookie = secureCookie;
    this.cookieSameSite = cookieSameSite;
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

// The JWT is delivered only in an HttpOnly cookie, not in the JSON response.
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request) {

    LoginResponse response = authService.login(request);
    ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", response.getToken())
            .httpOnly(true)
            .secure(secureCookie)
            .sameSite(cookieSameSite)
            .path("/")
            .maxAge(Duration.ofMillis(jwtService.getExpirationMillis()))
            .build();

    return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(response);
}

@GetMapping("/session")
public ResponseEntity<AuthSessionResponse> session(
        @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(authService.currentSession(userDetails.getUsername()));
}

@PostMapping("/logout")
public ResponseEntity<Void> logout() {
    ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", "")
            .httpOnly(true)
            .secure(secureCookie)
            .sameSite(cookieSameSite)
            .path("/")
            .maxAge(Duration.ZERO)
            .build();
    return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build();
}

@GetMapping("/csrf")
public ResponseEntity<Map<String, String>> csrf(CsrfToken csrfToken) {
    return ResponseEntity.ok(Map.of("token", csrfToken.getToken()));
}
}
