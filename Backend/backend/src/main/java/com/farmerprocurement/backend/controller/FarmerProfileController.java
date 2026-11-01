package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.FarmerProfileRequest;
import com.farmerprocurement.backend.dto.FarmerProfileResponse;
import com.farmerprocurement.backend.service.FarmerProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmers/profile")
@Valid 
public class FarmerProfileController {

    private final FarmerProfileService farmerProfileService;

    public FarmerProfileController(
            FarmerProfileService farmerProfileService) {
        this.farmerProfileService = farmerProfileService;
    }

    // Create farmer profile
    @PostMapping
    public ResponseEntity<FarmerProfileResponse> createProfile(
            @Valid @RequestBody FarmerProfileRequest request) {

        FarmerProfileResponse response =
                farmerProfileService.createProfile(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get logged-in farmer profile
    @GetMapping
    public ResponseEntity<FarmerProfileResponse> getProfile() {

        FarmerProfileResponse response =
                farmerProfileService.getProfile();

        return ResponseEntity.ok(response);
    }

    // Update logged-in farmer profile
    @PutMapping
    public ResponseEntity<FarmerProfileResponse> updateProfile(
            @Valid @RequestBody FarmerProfileRequest request) {

        FarmerProfileResponse response =
                farmerProfileService.updateProfile(request);

        return ResponseEntity.ok(response);
    }
}