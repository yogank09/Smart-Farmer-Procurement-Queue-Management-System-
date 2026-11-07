package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.LoginRequest;
import com.farmerprocurement.backend.dto.LoginResponse;
import com.farmerprocurement.backend.dto.AuthSessionResponse;
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

        // Save Date of Birth.
        pending.setDateOfBirth(request.getDateOfBirth());

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

    // STEP 2: Verify email OTP and create the permanent user.
    @Transactional
    public String verifyEmailOtp(String email, String otp) {

        email = email.trim().toLowerCase(Locale.ROOT);

        PendingRegistration pending =
                pendingRegistrationRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No pending registration found for this email"));

        LocalDateTime now = LocalDateTime.now();

        // Check registration expiry.
        if (pending.getExpiresAt() == null
                || !pending.getExpiresAt().isAfter(now)) {

            pendingRegistrationRepository.delete(pending);

            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Registration expired. Please register again.");
        }

        // Prevent duplicate user creation.
        if (pending.isEmailVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already verified.");
        }

        // Check OTP expiry.
        if (pending.getEmailOtpHash() == null
                || pending.getEmailOtpExpiresAt() == null
                || !pending.getEmailOtpExpiresAt().isAfter(now)) {

            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "OTP expired. Please register again.");
        }

        // Check maximum incorrect attempts.
        if (pending.getFailedAttempts() >= 5) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many incorrect attempts. Please register again.");
        }

        // Verify OTP.
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

        // Create permanent user.
        User user = new User();

        user.setFullName(pending.getFullName());
        user.setEmail(pending.getEmail());
        user.setPhoneNumber(pending.getPhoneNumber());

        // Copy Date of Birth.
        user.setDateOfBirth(pending.getDateOfBirth());

        // Password is already BCrypt-hashed in pending registration.
        user.setPassword(pending.getPasswordHash());

        // Set default role.
        user.setRole(User.Role.FARMER);

        // Email verified, phone verification postponed.
        user.setEmailVerified(true);
        user.setPhoneVerified(false);

        // Initialize login security fields.
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        // Save permanent user.
        userRepository.saveAndFlush(user);

        // Delete pending registration only after successful user save.
        pendingRegistrationRepository.delete(pending);

        return "Email verified successfully. Your account has been created.";
    }

    // STEP 3: Authenticate user and generate JWT.
    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        LocalDateTime now = LocalDateTime.now();

        // Check whether the account exists and is locked.
        User existingUser = userRepository.findByEmail(email)
                .orElse(null);

        if (existingUser != null) {

            if (existingUser.getLockedUntil() != null) {

                // Account is still locked.
                if (existingUser.getLockedUntil().isAfter(now)) {
                    throw new ResponseStatusException(
                            HttpStatus.LOCKED,
                            "Account temporarily locked due to too many failed login attempts. Please try again later.");
                }

                // Lock expired: reset failed attempts.
                existingUser.setFailedLoginAttempts(0);
                existingUser.setLockedUntil(null);

                userRepository.save(existingUser);
            }
        }

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

            // Reset failed attempts after successful login.
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            userRepository.save(user);

            // Generate JWT after successful authentication.
            String token = jwtService.generateToken(authenticatedUser);

            return new LoginResponse(
                    "Login successful",
                    token,
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole().name(),
                    user.getDateOfBirth());

        } catch (AuthenticationException exception) {

            // Increase failed login attempts.
            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user != null) {

                int attempts = user.getFailedLoginAttempts() + 1;

                user.setFailedLoginAttempts(attempts);

                // Lock account after five incorrect attempts.
                if (attempts >= 5) {

                    user.setLockedUntil(
                            LocalDateTime.now().plusMinutes(15));

                    userRepository.save(user);

                    throw new ResponseStatusException(
                            HttpStatus.LOCKED,
                            "Account temporarily locked for 15 minutes due to too many failed login attempts.");
                }

                userRepository.save(user);
            }

            // Generic error message.
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

    }

    @Transactional(readOnly = true)
    public AuthSessionResponse currentSession(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user no longer exists"));

        return new AuthSessionResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getRole().name());
    }
}
