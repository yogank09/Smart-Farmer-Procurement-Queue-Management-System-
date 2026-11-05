package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.ProcurementQueue;
import com.farmerprocurement.backend.entity.QueueStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcurementQueueRepository
        extends JpaRepository<ProcurementQueue, Long> {

    // Find all queue entries for a procurement slot
    List<ProcurementQueue> findByProcurementSlotIdOrderByQueuePositionAsc(
            Long slotId
    );

    // Find queue entries by slot and status
    List<ProcurementQueue> findByProcurementSlotIdAndStatusOrderByQueuePositionAsc(
            Long slotId,
            QueueStatus status
    );

    // Find queue entry using procurement token ID
    Optional<ProcurementQueue> findByProcurementTokenId(
            Long tokenId
    );

    // Find queue entries by farmers user ID
    List<ProcurementQueue> findByProcurementTokenFarmerUserId(
            Long userId
    );

    // Check  a token is already added to the queue
    boolean existsByProcurementTokenId(Long tokenId);

    // Count farmers currently waiting in a slot
    long countByProcurementSlotIdAndStatus(
            Long slotId,
            QueueStatus status
    );
}

