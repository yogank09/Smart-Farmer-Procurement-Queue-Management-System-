package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.FarmerCropRequest;
import com.farmerprocurement.backend.dto.FarmerCropResponse;
import com.farmerprocurement.backend.entity.Farmer;
import com.farmerprocurement.backend.entity.FarmerCrop;
import com.farmerprocurement.backend.repository.FarmerCropRepository;
import com.farmerprocurement.backend.repository.FarmerRepository;
import com.farmerprocurement.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FarmerCropService {

    private final FarmerCropRepository farmerCropRepository;
    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;

    public FarmerCropService(
            FarmerCropRepository farmerCropRepository,
            FarmerRepository farmerRepository,
            UserRepository userRepository
    ) {
        this.farmerCropRepository = farmerCropRepository;
        this.farmerRepository = farmerRepository;
        this.userRepository = userRepository;
    }

    // Get the currently authenticated farmer
    private Farmer getAuthenticatedFarmer() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        String email = authentication.getName().trim().toLowerCase();

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        if (!"FARMER".equals(user.getRole().name())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only farmers can manage crops"
            );
        }

        return farmerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farmer profile not found. Please create your profile first."
                ));
    }

    // Add a new crop
    @Transactional
    public FarmerCropResponse addCrop(FarmerCropRequest request) {

        Farmer farmer = getAuthenticatedFarmer();

        FarmerCrop crop = new FarmerCrop();

        crop.setFarmer(farmer);
        crop.setCropName(request.getCropName());
        crop.setSeason(request.getSeason());
        crop.setLandArea(request.getLandArea());
        crop.setLandAreaUnit(request.getLandAreaUnit());
        crop.setSowingDate(request.getSowingDate());
        crop.setExpectedQuantity(request.getExpectedQuantity());
        crop.setQuantityUnit(request.getQuantityUnit());

        FarmerCrop savedCrop = farmerCropRepository.save(crop);

        return mapToResponse(savedCrop);
    }

    // Get all crops belonging to the logged-in farmer
    @Transactional(readOnly = true)
    public List<FarmerCropResponse> getMyCrops() {

        Farmer farmer = getAuthenticatedFarmer();

        return farmerCropRepository.findByFarmerId(farmer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get a specific crop
    @Transactional(readOnly = true)
    public FarmerCropResponse getCropById(Long cropId) {

        Farmer farmer = getAuthenticatedFarmer();

        FarmerCrop crop = farmerCropRepository
                .findByIdAndFarmerId(cropId, farmer.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Crop not found"
                ));

        return mapToResponse(crop);
    }

    // Update an existing crop
    @Transactional
    public FarmerCropResponse updateCrop(
            Long cropId,
            FarmerCropRequest request
    ) {

        Farmer farmer = getAuthenticatedFarmer();

        FarmerCrop crop = farmerCropRepository
                .findByIdAndFarmerId(cropId, farmer.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Crop not found"
                ));

        crop.setCropName(request.getCropName());
        crop.setSeason(request.getSeason());
        crop.setLandArea(request.getLandArea());
        crop.setLandAreaUnit(request.getLandAreaUnit());
        crop.setSowingDate(request.getSowingDate());
        crop.setExpectedQuantity(request.getExpectedQuantity());
        crop.setQuantityUnit(request.getQuantityUnit());

        FarmerCrop updatedCrop = farmerCropRepository.save(crop);

        return mapToResponse(updatedCrop);
    }

    // Delete a crop
    @Transactional
    public void deleteCrop(Long cropId) {

        Farmer farmer = getAuthenticatedFarmer();

        FarmerCrop crop = farmerCropRepository
                .findByIdAndFarmerId(cropId, farmer.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Crop not found"
                ));

        farmerCropRepository.delete(crop);
    }

    // Convert FarmerCrop entity into response DTO
    private FarmerCropResponse mapToResponse(FarmerCrop crop) {

        FarmerCropResponse response = new FarmerCropResponse();

        response.setId(crop.getId());
        response.setFarmerId(crop.getFarmer().getId());
        response.setCropName(crop.getCropName());
        response.setSeason(crop.getSeason());
        response.setLandArea(crop.getLandArea());
        response.setLandAreaUnit(crop.getLandAreaUnit());
        response.setSowingDate(crop.getSowingDate());
        response.setExpectedQuantity(crop.getExpectedQuantity());
        response.setQuantityUnit(crop.getQuantityUnit());
        response.setCreatedAt(crop.getCreatedAt());

        return response;
    }
}

