package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.ProcurementTokenRequest;
import com.farmerprocurement.backend.dto.ProcurementTokenResponse;
import com.farmerprocurement.backend.service.ProcurementTokenService;
import org.springframework.web.bind.annotation.PatchMapping;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class ProcurementTokenController {

    private final ProcurementTokenService procurementTokenService;

    public ProcurementTokenController(
            ProcurementTokenService procurementTokenService) {

        this.procurementTokenService = procurementTokenService;
    }

    // Book a procurement token
    @PostMapping
    public ResponseEntity<ProcurementTokenResponse> bookToken(
            @Valid @RequestBody ProcurementTokenRequest request,
            Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        ProcurementTokenResponse response = procurementTokenService.bookToken(
                request,
                userDetails);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all tokens for the logged-in farmer
    @GetMapping("/my-tokens")
    public ResponseEntity<List<ProcurementTokenResponse>> getMyTokens(
            Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        List<ProcurementTokenResponse> response = procurementTokenService.getMyTokens(userDetails);

        return ResponseEntity.ok(response);
    }

    // Get a specific procurement token by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProcurementTokenResponse> getTokenById(
            @PathVariable Long id,
            Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        ProcurementTokenResponse response = procurementTokenService.getTokenById(
                id,
                userDetails);

        return ResponseEntity.ok(response);
    }

    // Cancel a procurement token
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ProcurementTokenResponse> cancelToken(
            @PathVariable Long id,
            Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        ProcurementTokenResponse response = procurementTokenService.cancelToken(
                id,
                userDetails);

        return ResponseEntity.ok(response);
    }

}
