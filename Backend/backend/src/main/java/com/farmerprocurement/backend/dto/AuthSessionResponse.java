package com.farmerprocurement.backend.dto;

import java.time.LocalDate;

public record AuthSessionResponse(
        Long userId,
        String fullName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        String role) {
}
