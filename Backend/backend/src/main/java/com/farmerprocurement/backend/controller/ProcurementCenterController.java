package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.ProcurementCenterRequest;
import com.farmerprocurement.backend.dto.ProcurementCenterResponse;
import com.farmerprocurement.backend.service.ProcurementCenterService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centers")
public class ProcurementCenterController {

    private final ProcurementCenterService centerService;

    public ProcurementCenterController(
            ProcurementCenterService centerService) {
        this.centerService = centerService;
    }

    // Create a procurement center
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcurementCenterResponse createCenter(
            @Valid @RequestBody ProcurementCenterRequest request) {

        return centerService.createCenter(request);
    }

    // Get all active centers
    @GetMapping
    public List<ProcurementCenterResponse> getAllCenters() {
        return centerService.getAllCenters();
    }

    // Get center by ID
    @GetMapping("/{id}")
    public ProcurementCenterResponse getCenterById(
            @PathVariable Long id) {

        return centerService.getCenterById(id);
    }

    // Update center
    @PutMapping("/{id}")
    public ProcurementCenterResponse updateCenter(
            @PathVariable Long id,
            @Valid @RequestBody ProcurementCenterRequest request) {

        return centerService.updateCenter(id, request);
    }

    // Deactivate center
    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateCenter(@PathVariable Long id) {
        centerService.deactivateCenter(id);
    }

    // Activate center
    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateCenter(@PathVariable Long id) {
        centerService.activateCenter(id);
    }

    // Search centers by district
    @GetMapping("/search/district")
    public List<ProcurementCenterResponse> getCentersByDistrict(
            @RequestParam String district) {

        return centerService.getCentersByDistrict(district);
    }

    // Search centers by state
    @GetMapping("/search/state")
    public List<ProcurementCenterResponse> getCentersByState(
            @RequestParam String state) {

        return centerService.getCentersByState(state);
    }
}