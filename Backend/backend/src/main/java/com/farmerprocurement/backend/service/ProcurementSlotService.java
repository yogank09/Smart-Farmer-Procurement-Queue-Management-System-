package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.ProcurementSlotRequest;
import com.farmerprocurement.backend.dto.ProcurementSlotResponse;
import com.farmerprocurement.backend.entity.ProcurementCenter;
import com.farmerprocurement.backend.entity.ProcurementSlot;
import com.farmerprocurement.backend.entity.SlotStatus;
import com.farmerprocurement.backend.repository.ProcurementCenterRepository;
import com.farmerprocurement.backend.repository.ProcurementSlotRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@Transactional
public class ProcurementSlotService {

    private final ProcurementSlotRepository procurementSlotRepository;
    private final ProcurementCenterRepository procurementCenterRepository;

    public ProcurementSlotService(
            ProcurementSlotRepository procurementSlotRepository,
            ProcurementCenterRepository procurementCenterRepository) {

        this.procurementSlotRepository = procurementSlotRepository;
        this.procurementCenterRepository = procurementCenterRepository;
    }

    public ProcurementSlotResponse createSlot(ProcurementSlotRequest request) {

        ProcurementCenter center = procurementCenterRepository
                .findById(request.getCenterId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Procurement center not found"));

        if (!center.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Procurement center is inactive");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End time must be after start time");
        }

        ProcurementSlot slot = new ProcurementSlot();

        slot.setProcurementCenter(center);
        slot.setCropName(request.getCropName());
        slot.setSlotDate(request.getSlotDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setMaxCapacity(request.getMaxCapacity());
        slot.setBookedCapacity(0);
        slot.setStatus(SlotStatus.AVAILABLE);

        ProcurementSlot savedSlot = procurementSlotRepository.save(slot);

        return mapToResponse(savedSlot);
    }

    private ProcurementSlotResponse mapToResponse(ProcurementSlot slot) {

        ProcurementSlotResponse response = new ProcurementSlotResponse();

        response.setId(slot.getId());
        response.setCenterId(slot.getProcurementCenter().getId());
        response.setCenterName(slot.getProcurementCenter().getCenterName());
        response.setCropName(slot.getCropName());
        response.setSlotDate(slot.getSlotDate());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setMaxCapacity(slot.getMaxCapacity());
        response.setBookedCapacity(slot.getBookedCapacity());

        response.setAvailableCapacity(
                slot.getMaxCapacity() - slot.getBookedCapacity());

        response.setStatus(slot.getStatus());
        response.setCreatedAt(slot.getCreatedAt());

        return response;
    }

    public List<ProcurementSlotResponse> getAllSlots() {

        List<ProcurementSlot> slots = procurementSlotRepository.findAll();

        return slots.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProcurementSlotResponse getSlotById(Long id) {

        ProcurementSlot slot = procurementSlotRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Procurement slot not found"));

        return mapToResponse(slot);
    }

    public List<ProcurementSlotResponse> getSlotsByCenter(Long centerId) {

        if (!procurementCenterRepository.existsById(centerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Procurement center not found");
        }

        List<ProcurementSlot> slots = procurementSlotRepository.findByProcurementCenterId(centerId);

        return slots.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProcurementSlotResponse> getSlotsByCenterAndDate(
            Long centerId,
            java.time.LocalDate slotDate) {

        if (!procurementCenterRepository.existsById(centerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Procurement center not found");
        }

        List<ProcurementSlot> slots = procurementSlotRepository
                .findByProcurementCenterIdAndSlotDate(centerId, slotDate);

        return slots.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProcurementSlotResponse> getSlotsByStatus(SlotStatus status) {

        List<ProcurementSlot> slots = procurementSlotRepository.findByStatus(status);

        return slots.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProcurementSlotResponse> getSlotsByCenterAndStatus(
            Long centerId, SlotStatus status) {

        if (!procurementCenterRepository.existsById(centerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Procurement center not found");
        }

        List<ProcurementSlot> slots = procurementSlotRepository
                .findByProcurementCenterIdAndStatus(centerId, status);

        return slots.stream()
                .map(this::mapToResponse)
                .toList();
    }

}
