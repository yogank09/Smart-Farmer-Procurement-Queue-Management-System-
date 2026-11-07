package com.farmerprocurement.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

public class LoginResponse {

private String message;
private String token;
private String tokenType;
private Long userId;
private String fullName;
private String email;
private String phoneNumber;
private String role;
private LocalDate dateOfBirth;

public LoginResponse() {
}

public LoginResponse(
        String message,
        String token,
        Long userId,
        String fullName,
        String email,
        String phoneNumber,
        String role,
        LocalDate dateOfBirth
) {
    this.message = message;
    this.token = token;
    this.tokenType = "Bearer";
    this.userId = userId;
    this.fullName = fullName;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.role = role;
    this.dateOfBirth = dateOfBirth;
}

public String getMessage() {
    return message;
}

@JsonIgnore
public String getToken() {
    return token;
}

public String getTokenType() {
    return tokenType;
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

public String getPhoneNumber() {
    return phoneNumber;
}

public String getRole() {
    return role;
}

public LocalDate getDateOfBirth() {
    return dateOfBirth;
}

}
