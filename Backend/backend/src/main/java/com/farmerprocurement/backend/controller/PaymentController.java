package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.PaymentRequest;
import com.farmerprocurement.backend.dto.PaymentResponse;
import com.farmerprocurement.backend.entity.PaymentStatus;
import com.farmerprocurement.backend.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @PathVariable PaymentStatus status) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByStatus(status));
    }
}
