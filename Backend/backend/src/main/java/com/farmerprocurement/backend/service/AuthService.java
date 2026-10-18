package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.LoginRequest;
import com.farmerprocurement.backend.dto.LoginResponse;
import com.farmerprocurement.backend.dto.RegisterRequest;
import com.farmerprocurement.backend.dto.RegisterResponse;

import com.farmerprocurement.backend.entity.PendingRegistration;
import com.farmerprocurement.backend.entity.User;

import com.farmerprocurement.backend.repository.PendingRegistrationRepository;
import com.farmerprocurement.backend.repository.UserRepository;

import com.farmerprocurement.backend.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {

private final UserRepository userRepository;
private final PendingRegistrationRepository pendingRegistrationRepository;
private final PasswordEncoder passwordEncoder;
private final EmailOtpService emailOtpService;
private final OtpAttemptService otpAttemptService;

private final JwtService jwtService;
private final AuthenticationManager authenticationManager;
private final UserDetailsService userDetailsService;

public AuthService(
        UserRepository userRepository,
        PendingRegistrationRepository pendingRegistrationRepository,
        PasswordEncoder passwordEncoder,
        EmailOtpService emailOtpService,
        OtpAttemptService otpAttemptService,
        JwtService jwtService,
        AuthenticationManager authenticationManager,
        UserDetailsService userDetailsService) {

    this.userRepository = userRepository;
    this.pendingRegistrationRepository = pendingRegistrationRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailOtpService = emailOtpService;
    this.otpAttemptService = otpAttemptService;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
}

// STEP 1: Register a farmer and send an email OTP.
@Transactional
public RegisterResponse register(RegisterRequest request) {

    String email = request.getEmail()
            .trim()
            .toLowerCase(Locale.ROOT);

    String phoneNumber = request.getPhoneNumber().trim();

    // Check existing permanent accounts.
    if (userRepository.existsByEmail(email)) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email is already registered");
    }

    if (userRepository.existsByPhoneNumber(phoneNumber)) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Phone number is already registered");
    }

    // Check existing pending registrations.
    if (pendingRegistrationRepository.existsByEmail(email)) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A registration for this email is already pending");
    }

    if (pendingRegistrationRepository.existsByPhoneNumber(phoneNumber)) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A registration for this phone number is already pending");
    }

    // Create a pending registration.
    PendingRegistration pending = new PendingRegistration();

    pending.setFullName(request.getFullName().trim());
    pending.setEmail(email);
    pending.setPhoneNumber(phoneNumber);

    // Hash the password before saving it.
    pending.setPasswordHash(
            passwordEncoder.encode(request.getPassword()));

    // Generate the email OTP.
    String otp = emailOtpService.generateOtp();

    // Store only the OTP hash.
    pending.setEmailOtpHash(
            emailOtpService.hashOtp(otp));

    // Email OTP expires after five minutes.
    pending.setEmailOtpExpiresAt(
            emailOtpService.getOtpExpiryTime());

    // Pending registration expires after 15 minutes.
    pending.setExpiresAt(
            LocalDateTime.now().plusMinutes(15));

    pending.setEmailVerified(false);
    pending.setPhoneVerified(false);
    pending.setFailedAttempts(0);

    // Save the pending registration, not a permanent User.
    PendingRegistration saved =
            pendingRegistrationRepository.save(pending);

    // Send the email OTP.
    emailOtpService.sendOtpEmail(saved.getEmail(), otp);

    return new RegisterResponse(
            "Registration started. Check your email for the verification OTP.",
            saved.getId(),
            saved.getFullName(),
            saved.getEmail());
}

// STEP 2: Verify the email OTP.
@Transactional
public String verifyEmailOtp(String email, String otp) {

    // Normalize the email.
    email = email.trim().toLowerCase(Locale.ROOT);

    // Find the pending registration.
    PendingRegistration pending =
            pendingRegistrationRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No pending registration found for this email"));

    LocalDateTime now = LocalDateTime.now();

    // Check whether the entire registration has expired.
    if (pending.getExpiresAt() == null
            || !pending.getExpiresAt().isAfter(now)) {

        pendingRegistrationRepository.delete(pending);

        throw new ResponseStatusException(
                HttpStatus.GONE,
                "Registration expired. Please register again.");
    }

    // Avoid re-verifying an already verified email.
    if (pending.isEmailVerified()) {
        return "Email is already verified.";
    }

    // Check OTP availability and expiry.
    if (pending.getEmailOtpHash() == null
            || pending.getEmailOtpExpiresAt() == null
            || !pending.getEmailOtpExpiresAt().isAfter(now)) {

        throw new ResponseStatusException(
                HttpStatus.GONE,
                "OTP expired. Please request a new OTP.");
    }

    // Limit incorrect attempts.
    if (pending.getFailedAttempts() >= 5) {
        throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many incorrect attempts. Please register again.");
    }

    // Compare the submitted OTP with the stored hash.
    if (!emailOtpService.verifyOtp(
            otp,
            pending.getEmailOtpHash())) {

        int attempts = otpAttemptService.recordFailedAttempt(
                pending.getId());

        if (attempts >= 5) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many incorrect attempts. Please register again.");
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Incorrect OTP. Attempts remaining: " + (5 - attempts));
    }

    // Mark the email as verified.
    pending.setEmailVerified(true);

    // Remove the used OTP hash and expiry.
    pending.setEmailOtpHash(null);
    pending.setEmailOtpExpiresAt(null);

    pendingRegistrationRepository.save(pending);

    return "Email verified successfully. Phone verification is still required.";
}

// STEP 3: Authenticate a registered user and generate a JWT.
public LoginResponse login(LoginRequest request) {

    String email = request.getEmail()
            .trim()
            .toLowerCase(Locale.ROOT);

    try {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                request.getPassword()));

        UserDetails authenticatedUser =
                (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        // Generate a JWT after successful authentication.
        String token = jwtService.generateToken(authenticatedUser);

        return new LoginResponse(
                "Login successful",
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name());

    } catch (AuthenticationException exception) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password");
    }
}


}
