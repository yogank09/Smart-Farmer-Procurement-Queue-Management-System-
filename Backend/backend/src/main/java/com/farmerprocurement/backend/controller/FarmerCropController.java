package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.FarmerCropRequest;
import com.farmerprocurement.backend.dto.FarmerCropResponse;
import com.farmerprocurement.backend.service.FarmerCropService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmers/crops")
public class FarmerCropController {

    private final FarmerCropService farmerCropService;

    public FarmerCropController(FarmerCropService farmerCropService) {
        this.farmerCropService = farmerCropService;
    }

    // Add a new crop
    @PostMapping
    public ResponseEntity<FarmerCropResponse> addCrop(
            @Valid @RequestBody FarmerCropRequest request
    ) {
        FarmerCropResponse response =
                farmerCropService.addCrop(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all crops of logged-in farmer
    @GetMapping
    public ResponseEntity<List<FarmerCropResponse>> getMyCrops() {

        return ResponseEntity.ok(
                farmerCropService.getMyCrops()
        );
    }

    // Get crop by ID
    @GetMapping("/{cropId}")
    public ResponseEntity<FarmerCropResponse> getCropById(
            @PathVariable Long cropId
    ) {
        return ResponseEntity.ok(
                farmerCropService.getCropById(cropId)
        );
    }

    // Update crop
    @PutMapping("/{cropId}")
    public ResponseEntity<FarmerCropResponse> updateCrop(
            @PathVariable Long cropId,
            @Valid @RequestBody FarmerCropRequest request
    ) {
        return ResponseEntity.ok(
                farmerCropService.updateCrop(cropId, request)
        );
    }

    // Delete crop
    @DeleteMapping("/{cropId}")
    public ResponseEntity<Void> deleteCrop(
            @PathVariable Long cropId
    ) {
        farmerCropService.deleteCrop(cropId);

        return ResponseEntity.noContent().build();
    }
}