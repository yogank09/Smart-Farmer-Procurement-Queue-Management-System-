package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.ProcurementTokenRequest;
import com.farmerprocurement.backend.dto.ProcurementTokenResponse;
import com.farmerprocurement.backend.entity.*;
import com.farmerprocurement.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProcurementTokenService {

        private final ProcurementTokenRepository procurementTokenRepository;
        private final FarmerRepository farmerRepository;
        private final ProcurementSlotRepository procurementSlotRepository;
        private final UserRepository userRepository;

        public ProcurementTokenService(
                        ProcurementTokenRepository procurementTokenRepository,
                        FarmerRepository farmerRepository,
                        ProcurementSlotRepository procurementSlotRepository,
                        UserRepository userRepository) {

                this.procurementTokenRepository = procurementTokenRepository;
                this.farmerRepository = farmerRepository;
                this.procurementSlotRepository = procurementSlotRepository;
                this.userRepository = userRepository;
        }

        // Book a procurement token
        public ProcurementTokenResponse bookToken(
                        ProcurementTokenRequest request,
                        UserDetails userDetails) {

                Long userId = getUserId(userDetails);

                Farmer farmer = farmerRepository
                                .findByUserId(userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Farmer profile not found"));

                ProcurementSlot slot = procurementSlotRepository
                                .findById(request.getSlotId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Procurement slot not found"));

                if (slot.getStatus() != SlotStatus.AVAILABLE) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Procurement slot is not available");
                }

                int availableCapacity = slot.getMaxCapacity() - slot.getBookedCapacity();

                if (request.getQuantity() > availableCapacity) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Insufficient slot capacity");
                }

                if (procurementTokenRepository
                                .existsByFarmerIdAndProcurementSlotId(
                                                farmer.getId(), slot.getId())) {

                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Farmer has already booked this slot");
                }

                ProcurementToken token = new ProcurementToken();

                token.setFarmer(farmer);
                token.setProcurementSlot(slot);
                token.setQuantity(request.getQuantity());
                token.setStatus(TokenStatus.BOOKED);
                token.setTokenNumber("TKN-" + UUID.randomUUID());

                // Update booked capacity
                slot.setBookedCapacity(
                                slot.getBookedCapacity() + request.getQuantity());

                // Mark slot FULL when capacity is reached
                if (slot.getBookedCapacity() >= slot.getMaxCapacity()) {
                        slot.setStatus(SlotStatus.FULL);
                }

                ProcurementToken savedToken = procurementTokenRepository.save(token);

                procurementSlotRepository.save(slot);

                return mapToResponse(savedToken);
        }

        // Get all procurement tokens for the logged-in farmer
        public List<ProcurementTokenResponse> getMyTokens(
                        UserDetails userDetails) {

                Long userId = getUserId(userDetails);

                farmerRepository
                                .findByUserId(userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Farmer profile not found"));

                return procurementTokenRepository
                                .findByFarmerUserId(userId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        // Get a specific token by ID for the logged-in farmer
        public ProcurementTokenResponse getTokenById(
                        Long tokenId,
                        UserDetails userDetails) {

                Long userId = getUserId(userDetails);

                ProcurementToken token = procurementTokenRepository
                                .findByIdAndFarmerUserId(tokenId, userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Procurement token not found"));

                return mapToResponse(token);
        }

        // Cancel a procurement token
        public ProcurementTokenResponse cancelToken(
                        Long tokenId,
                        UserDetails userDetails) {

                Long userId = getUserId(userDetails);

                ProcurementToken token = procurementTokenRepository
                                .findByIdAndFarmerUserId(tokenId, userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Procurement token not found"));

                if (token.getStatus() == TokenStatus.CANCELLED) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Token is already cancelled");
                }

                if (token.getStatus() == TokenStatus.COMPLETED
                                || token.getStatus() == TokenStatus.PROCESSING) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Token cannot be cancelled in its current status");
                }

                ProcurementSlot slot = token.getProcurementSlot();

                // Restore booked capacity
                slot.setBookedCapacity(
                                slot.getBookedCapacity() - token.getQuantity());

                // Reopen the slot if it was FULL
                if (slot.getStatus() == SlotStatus.FULL
                                && slot.getBookedCapacity() < slot.getMaxCapacity()) {
                        slot.setStatus(SlotStatus.AVAILABLE);
                }

                // Update token status
                token.setStatus(TokenStatus.CANCELLED);

                procurementSlotRepository.save(slot);

                ProcurementToken savedToken = procurementTokenRepository.save(token);

                return mapToResponse(savedToken);
        }

        // Get user ID using authenticated email
        private Long getUserId(UserDetails userDetails) {

                return userRepository
                                .findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "User not found"))
                                .getId();
        }

        // Convert Entity to Response DTO
        private ProcurementTokenResponse mapToResponse(
                        ProcurementToken token) {

                ProcurementTokenResponse response = new ProcurementTokenResponse();

                ProcurementSlot slot = token.getProcurementSlot();

                response.setId(token.getId());
                response.setTokenNumber(token.getTokenNumber());

                response.setFarmerId(token.getFarmer().getId());

                response.setSlotId(slot.getId());

                response.setCenterId(
                                slot.getProcurementCenter().getId());

                response.setCenterName(
                                slot.getProcurementCenter().getCenterName());

                response.setCropName(slot.getCropName());

                response.setSlotDate(slot.getSlotDate());

                response.setStartTime(slot.getStartTime());

                response.setEndTime(slot.getEndTime());

                response.setQuantity(token.getQuantity());

                response.setAvailableCapacity(
                                slot.getMaxCapacity() - slot.getBookedCapacity());

                response.setStatus(token.getStatus());

                response.setBookedAt(token.getBookedAt());

                return response;
        }
}
