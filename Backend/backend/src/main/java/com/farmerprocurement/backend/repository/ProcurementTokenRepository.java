package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.ProcurementToken;
import com.farmerprocurement.backend.entity.TokenStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcurementTokenRepository
        extends JpaRepository<ProcurementToken, Long> {

    // Find all tokens belonging to a farmer
    List<ProcurementToken> findByFarmerId(Long farmerId);

    // Find tokens associated with a procurement slot
    List<ProcurementToken> findByProcurementSlotId(Long slotId);

    // Find tokens by status
    List<ProcurementToken> findByStatus(TokenStatus status);

    // Check duplicate booking
    boolean existsByFarmerIdAndProcurementSlotId(
            Long farmerId,
            Long slotId
    );

    // Find tokens using farmer's user ID
    List<ProcurementToken> findByFarmerUserId(Long userId);

    // Find a specific token belonging to a farmer
    Optional<ProcurementToken> findByIdAndFarmerUserId(
            Long id,
            Long userId
    );
}

