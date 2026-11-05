package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.ProcurementQueueRequest;
import com.farmerprocurement.backend.dto.ProcurementQueueResponse;
import com.farmerprocurement.backend.dto.request.CallNextFarmerRequest;
import com.farmerprocurement.backend.service.ProcurementQueueService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class ProcurementQueueController {

    private final ProcurementQueueService procurementQueueService;

    public ProcurementQueueController(
            ProcurementQueueService procurementQueueService) {

        this.procurementQueueService = procurementQueueService;
    }

    // Farmer joins the procurement queue
    @PostMapping("/join")
    public ResponseEntity<ProcurementQueueResponse> joinQueue(
            @Valid @RequestBody ProcurementQueueRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProcurementQueueResponse response =
                procurementQueueService.joinQueue(request, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Farmer views their own queue entries
    @GetMapping("/my-queue")
    public ResponseEntity<List<ProcurementQueueResponse>> getMyQueue(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ProcurementQueueResponse> responses =
                procurementQueueService.getMyQueue(userDetails);

        return ResponseEntity.ok(responses);
    }

    // Get queue entry by ID
    @GetMapping("/{queueId}")
    public ResponseEntity<ProcurementQueueResponse> getQueueById(
            @PathVariable Long queueId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProcurementQueueResponse response =
                procurementQueueService.getQueueById(queueId, userDetails);

        return ResponseEntity.ok(response);
    }

    // View all queue entries for a particular procurement slot
    @GetMapping("/slot/{slotId}")
    public ResponseEntity<List<ProcurementQueueResponse>> getQueueBySlot(
            @PathVariable Long slotId) {

        List<ProcurementQueueResponse> responses =
                procurementQueueService.getQueueBySlot(slotId);

        return ResponseEntity.ok(responses);
    }

    // Center Operator/Admin calls the next waiting farmer
    @PostMapping("/call-next")
    public ResponseEntity<ProcurementQueueResponse> callNextFarmer(
            @Valid @RequestBody CallNextFarmerRequest request) {

        ProcurementQueueResponse response =
                procurementQueueService.callNextFarmer(request.getSlotId());

        return ResponseEntity.ok(response);
    }

    // Center Operator/Admin starts farmer procurement processing
    @PatchMapping("/{queueId}/start-processing")
    public ResponseEntity<ProcurementQueueResponse> startProcessing(
            @PathVariable Long queueId) {

        ProcurementQueueResponse response =
                procurementQueueService.startProcessing(queueId);

        return ResponseEntity.ok(response);
    }

    // Center Operator/Admin completes farmer procurement
    @PatchMapping("/{queueId}/complete")
    public ResponseEntity<ProcurementQueueResponse> completeProcurement(
            @PathVariable Long queueId) {

        ProcurementQueueResponse response =
                procurementQueueService.completeProcurement(queueId);

        return ResponseEntity.ok(response);
    }
}

