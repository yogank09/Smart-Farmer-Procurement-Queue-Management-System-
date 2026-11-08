package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.entity.User;
import com.farmerprocurement.backend.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.Locale;

@Service
public class AdminProvisioningService {

    private static final String BOOTSTRAP_LOCK = "kisanmitra-admin-bootstrap";

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminProvisioningService(
            JdbcTemplate jdbcTemplate,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public long provisionFirstAdmin(
            String fullName,
            String email,
            String phoneNumber,
            LocalDate dateOfBirth,
            char[] password) {
        Integer lockAcquired = jdbcTemplate.queryForObject(
                "SELECT GET_LOCK(?, 0)",
                Integer.class,
                BOOTSTRAP_LOCK);
        if (!Integer.valueOf(1).equals(lockAcquired)) {
            throw new IllegalStateException("Another admin bootstrap is in progress.");
        }

        boolean releaseAfterTransaction = false;
        try {
            if (userRepository.existsByRole(User.Role.ADMIN)) {
                throw new IllegalStateException(
                        "An administrator already exists; first-admin bootstrap is disabled.");
            }

            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            String normalizedPhone = phoneNumber.trim();
            if (userRepository.existsByEmail(normalizedEmail)
                    || userRepository.existsByPhoneNumber(normalizedPhone)) {
                throw new IllegalArgumentException(
                        "The email address or phone number is already registered.");
            }

            User user = new User();
            user.setFullName(fullName.trim());
            user.setEmail(normalizedEmail);
            user.setPhoneNumber(normalizedPhone);
            user.setDateOfBirth(dateOfBirth);
            user.setPassword(passwordEncoder.encode(new String(password)));
            user.setRole(User.Role.ADMIN);
            user.setEmailVerified(true);
            user.setPhoneVerified(true);
            user.setFailedLoginAttempts(0);

            User saved = userRepository.saveAndFlush(user);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCompletion(int status) {
                                releaseBootstrapLock();
                            }
                        });
                releaseAfterTransaction = true;
            }
            return saved.getId();
        } finally {
            if (!releaseAfterTransaction) {
                releaseBootstrapLock();
            }
        }
    }

    private void releaseBootstrapLock() {
        jdbcTemplate.queryForObject(
                "SELECT RELEASE_LOCK(?)",
                Integer.class,
                BOOTSTRAP_LOCK);
    }
}
