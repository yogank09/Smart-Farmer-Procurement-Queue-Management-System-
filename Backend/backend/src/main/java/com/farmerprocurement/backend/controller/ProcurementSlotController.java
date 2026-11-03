package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.ProcurementSlotRequest;
import com.farmerprocurement.backend.dto.ProcurementSlotResponse;
import com.farmerprocurement.backend.entity.SlotStatus;
import com.farmerprocurement.backend.service.ProcurementSlotService;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class ProcurementSlotController {

    private final ProcurementSlotService procurementSlotService;

    public ProcurementSlotController(
            ProcurementSlotService procurementSlotService) {
        this.procurementSlotService = procurementSlotService;
    }

    // Create new procurement slot
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcurementSlotResponse createSlot(
            @Valid @RequestBody ProcurementSlotRequest request) {
        return procurementSlotService.createSlot(request);
    }

    // Get all slots
    @GetMapping
    public List<ProcurementSlotResponse> getAllSlots() {
        return procurementSlotService.getAllSlots();
    }

    // Get slot by ID
    @GetMapping("/{id}")
    public ProcurementSlotResponse getSlotById(@PathVariable Long id) {
        return procurementSlotService.getSlotById(id);
    }

    // Get slots by center
    @GetMapping("/center/{centerId}")
    public List<ProcurementSlotResponse> getSlotsByCenter(
            @PathVariable Long centerId) {
        return procurementSlotService.getSlotsByCenter(centerId);
    }

    // slots get by center and date
    @GetMapping("/center/{centerId}/date")
    public List<ProcurementSlotResponse> getSlotsByCenterAndDate(
            @PathVariable Long centerId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate slotDate) {

        return procurementSlotService
                .getSlotsByCenterAndDate(centerId, slotDate);
    }

    // Get slots by status
    @GetMapping("/status/{status}")
    public List<ProcurementSlotResponse> getSlotsByStatus(
            @PathVariable SlotStatus status) {
        return procurementSlotService.getSlotsByStatus(status);
    }

    // Get slots by center and status
    @GetMapping("/center/{centerId}/status/{status}")
    public List<ProcurementSlotResponse> getSlotsByCenterAndStatus(
            @PathVariable Long centerId,
            @PathVariable SlotStatus status) {

        return procurementSlotService
                .getSlotsByCenterAndStatus(centerId, status);
    }
}