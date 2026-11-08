package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.PaymentRequest;
import com.farmerprocurement.backend.dto.PaymentResponse;
import com.farmerprocurement.backend.entity.Payment;
import com.farmerprocurement.backend.entity.PaymentStatus;
import com.farmerprocurement.backend.entity.Procurement;
import com.farmerprocurement.backend.entity.ProcurementStatus;
import com.farmerprocurement.backend.entity.User;
import com.farmerprocurement.backend.repository.PaymentRepository;
import com.farmerprocurement.backend.repository.ProcurementRepository;
import com.farmerprocurement.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProcurementRepository procurementRepository;
    private final UserRepository userRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ProcurementRepository procurementRepository,
            UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.procurementRepository = procurementRepository;
        this.userRepository = userRepository;
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
    public List<PaymentResponse> getMyPayments(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        }
        User user = userRepository.findByEmail(
                        userDetails.getUsername().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first"));
        if (user.getRole() != User.Role.FARMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only farmers can view their payment history");
        }
        return paymentRepository.findByProcurement_ProcurementToken_Farmer_User_Id(user.getId())
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
