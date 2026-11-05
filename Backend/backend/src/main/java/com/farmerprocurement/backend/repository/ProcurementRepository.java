package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.Procurement;
import com.farmerprocurement.backend.entity.ProcurementStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcurementRepository extends JpaRepository<Procurement, Long> {


// Find procurement using its token ID
Optional<Procurement> findByProcurementToken_Id(Long tokenId);

// Find procurements by status
List<Procurement> findByStatus(ProcurementStatus status);


}
