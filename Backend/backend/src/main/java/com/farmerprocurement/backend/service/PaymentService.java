package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.PaymentRequest;
import com.farmerprocurement.backend.dto.PaymentResponse;
import com.farmerprocurement.backend.entity.Payment;
import com.farmerprocurement.backend.entity.PaymentStatus;
import com.farmerprocurement.backend.entity.Procurement;
import com.farmerprocurement.backend.entity.ProcurementStatus;
import com.farmerprocurement.backend.repository.PaymentRepository;
import com.farmerprocurement.backend.repository.ProcurementRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProcurementRepository procurementRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ProcurementRepository procurementRepository) {
        this.paymentRepository = paymentRepository;
        this.procurementRepository = procurementRepository;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {

        Procurement procurement = procurementRepository
                .findById(request.getProcurementId())
                .orElseThrow(() -> new RuntimeException(
                        "Procurement not found with ID: "
                                + request.getProcurementId()));

        if (procurement.getStatus() != ProcurementStatus.COMPLETED) {
            throw new RuntimeException(
                    "Payment can only be created for completed procurement");
        }

        paymentRepository.findByProcurement_Id(procurement.getId())
                .ifPresent(existingPayment -> {
                    throw new RuntimeException(
                            "Payment already exists for this procurement");
                });

        Payment payment = new Payment();

        payment.setProcurement(procurement);
        payment.setAmount(procurement.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod().trim().toUpperCase());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found with ID: " + paymentId));

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {

        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentResponse mapToResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setId(payment.getId());
        response.setProcurementId(payment.getProcurement().getId());
        response.setFarmerId(
                payment.getProcurement()
                        .getProcurementToken()
                        .getFarmer()
                        .getId());

        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionReference(payment.getTransactionReference());
        response.setPaymentDate(payment.getPaymentDate());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }
}
