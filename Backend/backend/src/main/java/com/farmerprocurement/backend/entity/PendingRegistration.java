package com.farmerprocurement.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(
    name = "pending_registrations",
    indexes = {
        @Index(name = "idx_pending_email", columnList = "email"),
        @Index(name = "idx_pending_phone", columnList = "phone_number")
    }
)
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 16)
    private String phoneNumber;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    // Store a BCrypt password hash, never the original password.
    @Column(nullable = false, length = 100)
    private String passwordHash;

    // Store hashes of OTPs, not the OTPs themselves.
    @Column(name = "email_otp_hash", length = 255)
    private String emailOtpHash;

    @Column(name = "phone_otp_hash", length = 255)
    private String phoneOtpHash;

    @Column(name = "email_otp_expires_at")
    private LocalDateTime emailOtpExpiresAt;

    @Column(name = "phone_otp_expires_at")
    private LocalDateTime phoneOtpExpiresAt;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public PendingRegistration() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmailOtpHash() {
        return emailOtpHash;
    }

    public void setEmailOtpHash(String emailOtpHash) {
        this.emailOtpHash = emailOtpHash;
    }

    public String getPhoneOtpHash() {
        return phoneOtpHash;
    }

    public void setPhoneOtpHash(String phoneOtpHash) {
        this.phoneOtpHash = phoneOtpHash;
    }

    public LocalDateTime getEmailOtpExpiresAt() {
        return emailOtpExpiresAt;
    }

    public void setEmailOtpExpiresAt(LocalDateTime emailOtpExpiresAt) {
        this.emailOtpExpiresAt = emailOtpExpiresAt;
    }

    public LocalDateTime getPhoneOtpExpiresAt() {
        return phoneOtpExpiresAt;
    }

    public void setPhoneOtpExpiresAt(LocalDateTime phoneOtpExpiresAt) {
        this.phoneOtpExpiresAt = phoneOtpExpiresAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
