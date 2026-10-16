package com.farmerprocurement.backend.dto;

public class RegisterResponse {

    private String message;
    private Long userId;
    private String fullName;
    private String email;

    public RegisterResponse() {
    }

    public RegisterResponse(
            String message,
            Long userId,
            String fullName,
            String email) {
        this.message = message;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}