package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.ProcurementRequest;
import com.farmerprocurement.backend.dto.ProcurementResponse;
import com.farmerprocurement.backend.entity.Procurement;
import com.farmerprocurement.backend.entity.ProcurementStatus;
import com.farmerprocurement.backend.entity.ProcurementToken;
import com.farmerprocurement.backend.entity.TokenStatus;
import com.farmerprocurement.backend.repository.ProcurementRepository;
import com.farmerprocurement.backend.repository.ProcurementTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcurementService {

    private final ProcurementRepository procurementRepository;
    private final ProcurementTokenRepository procurementTokenRepository;

    public ProcurementService(
            ProcurementRepository procurementRepository,
            ProcurementTokenRepository procurementTokenRepository) {

        this.procurementRepository = procurementRepository;
        this.procurementTokenRepository = procurementTokenRepository;
    }

    // Create procurement record
    @Transactional
    public ProcurementResponse createProcurement(ProcurementRequest request) {

        ProcurementToken token = procurementTokenRepository
                .findById(request.getTokenId())
                .orElseThrow(() -> new RuntimeException("Procurement token not found"));

        // Token must be completed in the queue before procurement is recorded
        if (token.getStatus() != TokenStatus.COMPLETED) {
            throw new RuntimeException(
                    "Procurement can only be recorded after queue completion");
        }

        // Prevent duplicate procurement for the same token
        if (procurementRepository
                .findByProcurementToken_Id(token.getId())
                .isPresent()) {

            throw new RuntimeException(
                    "Procurement record already exists for this token");
        }

        // Convert KG into quintals
        BigDecimal quantityInQuintals = request.getActualQuantity()
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Calculate total procurement amount
        BigDecimal totalAmount = quantityInQuintals
                .multiply(request.getRatePerQuintal())
                .setScale(2, RoundingMode.HALF_UP);

        Procurement procurement = new Procurement();

        procurement.setProcurementToken(token);
        procurement.setActualQuantity(request.getActualQuantity());
        procurement.setQualityGrade(request.getQualityGrade());
        procurement.setRatePerQuintal(request.getRatePerQuintal());
        procurement.setTotalAmount(totalAmount);

        procurement.setStatus(ProcurementStatus.COMPLETED);
        procurement.setCompletedAt(LocalDateTime.now());

        Procurement savedProcurement = procurementRepository.save(procurement);

        return mapToResponse(savedProcurement);
    }

    // Get procurement by ID
    @Transactional(readOnly = true)
    public ProcurementResponse getProcurementById(Long procurementId) {

        Procurement procurement = procurementRepository
                .findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement record not found"));

        return mapToResponse(procurement);
    }

    // Get all procurement records
    @Transactional(readOnly = true)
    public List<ProcurementResponse> getAllProcurements() {

        return procurementRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get procurement records by status
    @Transactional(readOnly = true)
    public List<ProcurementResponse> getProcurementsByStatus(
            ProcurementStatus status) {

        return procurementRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Convert entity into response DTO
    private ProcurementResponse mapToResponse(
            Procurement procurement) {

        ProcurementToken token = procurement.getProcurementToken();

        ProcurementResponse response = new ProcurementResponse();

        response.setId(procurement.getId());
        response.setTokenId(token.getId());

        response.setFarmerId(token.getFarmer().getId());

        response.setCropName(
                token.getProcurementSlot().getCropName());

        response.setCenterId(
                token.getProcurementSlot()
                        .getProcurementCenter()
                        .getId());

        response.setActualQuantity(
                procurement.getActualQuantity());

        response.setQualityGrade(
                procurement.getQualityGrade());

        response.setRatePerQuintal(
                procurement.getRatePerQuintal());

        response.setTotalAmount(
                procurement.getTotalAmount());

        response.setStatus(procurement.getStatus());

        response.setCreatedAt(procurement.getCreatedAt());
        response.setUpdatedAt(procurement.getUpdatedAt());
        response.setCompletedAt(procurement.getCompletedAt());

        return response;
    }

}
