package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.RegisterRequest;
import com.farmerprocurement.backend.dto.RegisterResponse;
import com.farmerprocurement.backend.entity.User;
import com.farmerprocurement.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // Normalize email and phone number.
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        String phoneNumber = request.getPhoneNumber().trim();

        // Check whether the email is already registered.
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        // Check whether the phone number is already registered.
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone number is already registered"
            );
        }

        // Create a new farmer.
        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);

        // Never store the plain-text password.
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(User.Role.FARMER);

        // Require verification before enabling login.
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        // Save the farmer in MySQL.
        User savedUser = userRepository.save(user);

        // Return safe information without the password.
        return new RegisterResponse(
                "Account created. Email and phone verification are required.",
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail()
        );
    }
}