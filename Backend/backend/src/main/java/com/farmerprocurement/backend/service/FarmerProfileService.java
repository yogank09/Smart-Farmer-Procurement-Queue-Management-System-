package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.FarmerProfileRequest;
import com.farmerprocurement.backend.dto.FarmerProfileResponse;
import com.farmerprocurement.backend.entity.Farmer;
import com.farmerprocurement.backend.entity.User;
import com.farmerprocurement.backend.repository.FarmerRepository;
import com.farmerprocurement.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FarmerProfileService {

    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;

    public FarmerProfileService(
            FarmerRepository farmerRepository,
            UserRepository userRepository) {
        this.farmerRepository = farmerRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Please login first");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public FarmerProfileResponse createProfile(FarmerProfileRequest request) {

        User user = getAuthenticatedUser();

        if (user.getRole() != User.Role.FARMER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only farmers can create farmer profiles");
        }

        if (farmerRepository.existsByUserId(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Farmer profile already exists");
        }

        if (request.getFarmerId() != null
                && !request.getFarmerId().isBlank()
                && farmerRepository.existsByFarmerId(request.getFarmerId().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Farmer ID already exists");
        }

        Farmer farmer = new Farmer();

        farmer.setUser(user);
        farmer.setVillage(request.getVillage().trim());
        farmer.setDistrict(request.getDistrict().trim());
        farmer.setState(request.getState().trim());
        farmer.setAddress(request.getAddress().trim());
        farmer.setFarmerId(normalizeFarmerId(request.getFarmerId()));
        farmer.setLandArea(request.getLandArea());
        farmer.setLandAreaUnit(request.getLandAreaUnit().trim());

        farmerRepository.save(farmer);

        return mapToResponse(farmer);
    }

    @Transactional(readOnly = true)
    public FarmerProfileResponse getProfile() {

        User user = getAuthenticatedUser();

        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farmer profile not found"));

        return mapToResponse(farmer);
    }

    @Transactional
    public FarmerProfileResponse updateProfile(FarmerProfileRequest request) {

        User user = getAuthenticatedUser();

        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farmer profile not found"));

        if (request.getFarmerId() != null
                && !request.getFarmerId().isBlank()
                && !request.getFarmerId().trim().equals(farmer.getFarmerId())
                && farmerRepository.existsByFarmerId(request.getFarmerId().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Farmer ID already exists");
        }

        farmer.setVillage(request.getVillage().trim());
        farmer.setDistrict(request.getDistrict().trim());
        farmer.setState(request.getState().trim());
        farmer.setAddress(request.getAddress().trim());
        farmer.setFarmerId(normalizeFarmerId(request.getFarmerId()));
        farmer.setLandArea(request.getLandArea());
        farmer.setLandAreaUnit(request.getLandAreaUnit().trim());

        return mapToResponse(farmerRepository.save(farmer));
    }

    private String normalizeFarmerId(String farmerId) {
        if (farmerId == null || farmerId.isBlank()) {
            return null;
        }
        return farmerId.trim();
    }

    private FarmerProfileResponse mapToResponse(Farmer farmer) {

        User user = farmer.getUser();

        FarmerProfileResponse response = new FarmerProfileResponse();

        response.setId(farmer.getId());
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());

        response.setVillage(farmer.getVillage());
        response.setDistrict(farmer.getDistrict());
        response.setState(farmer.getState());
        response.setAddress(farmer.getAddress());
        response.setFarmerId(farmer.getFarmerId());
        response.setLandArea(farmer.getLandArea());
        response.setLandAreaUnit(farmer.getLandAreaUnit());

        return response;
    }
}