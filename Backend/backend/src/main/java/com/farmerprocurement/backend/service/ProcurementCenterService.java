package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.ProcurementCenterRequest;
import com.farmerprocurement.backend.dto.ProcurementCenterResponse;
import com.farmerprocurement.backend.entity.ProcurementCenter;
import com.farmerprocurement.backend.repository.ProcurementCenterRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProcurementCenterService {

    private final ProcurementCenterRepository centerRepository;

    public ProcurementCenterService(
            ProcurementCenterRepository centerRepository) {
        this.centerRepository = centerRepository;
    }

    // Create a procurement center
    public ProcurementCenterResponse createCenter(
            ProcurementCenterRequest request) {

        String centerCode = request.getCenterCode().trim().toUpperCase();

        if (centerRepository.existsByCenterCode(centerCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Center code already exists"
            );
        }

        ProcurementCenter center = new ProcurementCenter();

        validateCoordinates(request);
        mapRequestToEntity(request, center);
        center.setCenterCode(centerCode);
        center.setActive(true);

        ProcurementCenter savedCenter = centerRepository.save(center);

        return mapToResponse(savedCenter);
    }

    // Get all active centers
    public List<ProcurementCenterResponse> getAllCenters() {
        return centerRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get center by ID
    public ProcurementCenterResponse getCenterById(Long centerId) {

        ProcurementCenter center = centerRepository.findById(centerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Procurement center not found"
                ));

        return mapToResponse(center);
    }

    // Update center details
    @Transactional
    public ProcurementCenterResponse updateCenter(
            Long centerId,
            ProcurementCenterRequest request) {

        ProcurementCenter center = centerRepository.findById(centerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Procurement center not found"
                ));

        String centerCode = request.getCenterCode().trim().toUpperCase();

        boolean codeUsed = centerRepository.findByCenterCode(centerCode)
                .filter(existing -> !existing.getId().equals(centerId))
                .isPresent();

        if (codeUsed) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Center code already exists"
            );
        }

        validateCoordinates(request);
        mapRequestToEntity(request, center);
        center.setCenterCode(centerCode);

        return mapToResponse(centerRepository.save(center));
    }

    // Deactivate a center
    @Transactional
    public void deactivateCenter(Long centerId) {

        ProcurementCenter center = centerRepository.findById(centerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Procurement center not found"
                ));

        center.setActive(false);
        centerRepository.save(center);
    }

    // Activate a center
    @Transactional
    public void activateCenter(Long centerId) {

        ProcurementCenter center = centerRepository.findById(centerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Procurement center not found"
                ));

        center.setActive(true);
        centerRepository.save(center);
    }

    // Search centers by district
    public List<ProcurementCenterResponse> getCentersByDistrict(
            String district) {

        return centerRepository
                .findByDistrictIgnoreCaseAndActiveTrue(district.trim())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Search centers by state
    public List<ProcurementCenterResponse> getCentersByState(
            String state) {

        return centerRepository
                .findByStateIgnoreCaseAndActiveTrue(state.trim())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Map request DTO to entity
    private void mapRequestToEntity(
            ProcurementCenterRequest request,
            ProcurementCenter center) {

        center.setCenterName(request.getCenterName().trim());
        center.setAddress(request.getAddress().trim());
        center.setVillage(request.getVillage().trim());
        center.setDistrict(request.getDistrict().trim());
        center.setState(request.getState().trim());
        center.setPincode(request.getPincode());
        center.setContactNumber(request.getContactNumber());
        if (request.getLatitude() != null && request.getLongitude() != null) {
            center.setLatitude(request.getLatitude());
            center.setLongitude(request.getLongitude());
        }
    }

    // Map entity to response DTO
    private ProcurementCenterResponse mapToResponse(
            ProcurementCenter center) {

        ProcurementCenterResponse response =
                new ProcurementCenterResponse();

        response.setId(center.getId());
        response.setCenterName(center.getCenterName());
        response.setCenterCode(center.getCenterCode());
        response.setAddress(center.getAddress());
        response.setVillage(center.getVillage());
        response.setDistrict(center.getDistrict());
        response.setState(center.getState());
        response.setPincode(center.getPincode());
        response.setContactNumber(center.getContactNumber());
        response.setLatitude(center.getLatitude());
        response.setLongitude(center.getLongitude());
        response.setActive(center.isActive());
        response.setCreatedAt(center.getCreatedAt());

        return response;
    }

    private void validateCoordinates(ProcurementCenterRequest request) {
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();
        if ((latitude == null) != (longitude == null)
                || (latitude != null && (!Double.isFinite(latitude) || latitude < -90 || latitude > 90))
                || (longitude != null && (!Double.isFinite(longitude) || longitude < -180 || longitude > 180))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide both valid center coordinates or omit both"
            );
        }
    }
}