package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> testEmail(
            @RequestParam String to) {

        emailService.sendEmail(
                to,
                "Smart Farmer System - Email Test",
                "Congratulations! Your Gmail SMTP configuration is working."
        );

        return ResponseEntity.ok("Test email sent successfully.");
    }
}