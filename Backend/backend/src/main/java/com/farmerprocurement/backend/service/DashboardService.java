package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.DashboardSummaryResponse;
import com.farmerprocurement.backend.entity.Payment;
import com.farmerprocurement.backend.entity.Procurement;
import com.farmerprocurement.backend.entity.ProcurementStatus;
import com.farmerprocurement.backend.repository.FarmerRepository;
import com.farmerprocurement.backend.repository.PaymentRepository;
import com.farmerprocurement.backend.repository.ProcurementRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final FarmerRepository farmerRepository;
    private final ProcurementRepository procurementRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(
            FarmerRepository farmerRepository,
            ProcurementRepository procurementRepository,
            PaymentRepository paymentRepository) {

        this.farmerRepository = farmerRepository;
        this.procurementRepository = procurementRepository;
        this.paymentRepository = paymentRepository;
    }

    public DashboardSummaryResponse getDashboardSummary() {

        DashboardSummaryResponse response = new DashboardSummaryResponse();

        // Total farmers
        response.setTotalFarmers(farmerRepository.count());

        // Procurement statistics
        List<Procurement> procurements = procurementRepository.findAll();

        response.setTotalProcurements(procurements.size());

        long completedProcurements = procurements.stream()
                .filter(procurement ->
                        procurement.getStatus() == ProcurementStatus.COMPLETED)
                .count();

        response.setCompletedProcurements(completedProcurements);

        BigDecimal totalProcurementAmount = procurements.stream()
                .map(Procurement::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalProcurementAmount(totalProcurementAmount);

        // Payment statistics
        List<Payment> payments = paymentRepository.findAll();

        response.setTotalPayments(payments.size());

        long completedPayments = payments.stream()
                .filter(payment ->
                        payment.getStatus().name().equals("COMPLETED"))
                .count();

        response.setCompletedPayments(completedPayments);

        BigDecimal totalPaymentAmount = payments.stream()
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalPaymentAmount(totalPaymentAmount);

        return response;
    }
}