package com.farmerprocurement.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailOtpService {

    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmailOtpService(
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateOtp() {
        int otp = secureRandom.nextInt(1_000_000);
        return String.format("%06d", otp);
    }

    public String hashOtp(String otp) {
        return passwordEncoder.encode(otp);
    }

    public boolean verifyOtp(String otp, String otpHash) {
        return passwordEncoder.matches(otp, otpHash);
    }

    public LocalDateTime getOtpExpiryTime() {
        return LocalDateTime.now().plusMinutes(5);
    }

    public void sendOtpEmail(String email, String otp) {
        String subject = "Smart Farmer System - Email Verification";

        String body = """
                Hello,

                Your email verification OTP for the Smart Farmer Procurement
                and Queue Management System is:

                %s

                This OTP expires in 5 minutes.

                Do not share this code with anyone.

                If you did not request this code, please ignore this email.

                Regards,
                Smart Farmer System
                """.formatted(otp);

        emailService.sendEmail(email, subject, body);
    }
}