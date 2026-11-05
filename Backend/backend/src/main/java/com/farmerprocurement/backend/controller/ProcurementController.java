package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.ProcurementRequest;
import com.farmerprocurement.backend.dto.ProcurementResponse;
import com.farmerprocurement.backend.entity.ProcurementStatus;
import com.farmerprocurement.backend.service.ProcurementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurements")
public class ProcurementController {


private final ProcurementService procurementService;

public ProcurementController(ProcurementService procurementService) {
    this.procurementService = procurementService;
}

// Create procurement record
@PostMapping
public ResponseEntity<ProcurementResponse> createProcurement(
        @Valid @RequestBody ProcurementRequest request) {

    ProcurementResponse response =
            procurementService.createProcurement(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
}

// Get all procurement records
@GetMapping
public ResponseEntity<List<ProcurementResponse>> getAllProcurements() {

    return ResponseEntity.ok(
            procurementService.getAllProcurements());
}

// Get procurement by ID
@GetMapping("/{procurementId}")
public ResponseEntity<ProcurementResponse> getProcurementById(
        @PathVariable Long procurementId) {

    return ResponseEntity.ok(
            procurementService.getProcurementById(procurementId));
}

// Get procurement records by status
@GetMapping("/status/{status}")
public ResponseEntity<List<ProcurementResponse>> getProcurementsByStatus(
        @PathVariable ProcurementStatus status) {

    return ResponseEntity.ok(
            procurementService.getProcurementsByStatus(status));
}


}
