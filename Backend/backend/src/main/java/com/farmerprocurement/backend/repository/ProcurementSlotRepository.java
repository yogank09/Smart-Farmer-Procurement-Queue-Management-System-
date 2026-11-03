package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.ProcurementSlot;
import com.farmerprocurement.backend.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProcurementSlotRepository
        extends JpaRepository<ProcurementSlot, Long> {

    List<ProcurementSlot> findByProcurementCenterId(Long centerId);

    List<ProcurementSlot> findByProcurementCenterIdAndSlotDate(
            Long centerId,
            LocalDate slotDate
    );

    List<ProcurementSlot> findByStatus(SlotStatus status);

    List<ProcurementSlot> findByProcurementCenterIdAndStatus(
            Long centerId,
            SlotStatus status
    );
}

