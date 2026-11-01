package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.ProcurementCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcurementCenterRepository
        extends JpaRepository<ProcurementCenter, Long> {

    Optional<ProcurementCenter> findByCenterCode(String centerCode);

    boolean existsByCenterCode(String centerCode);

    List<ProcurementCenter> findByDistrictIgnoreCaseAndActiveTrue(String district);

    List<ProcurementCenter> findByStateIgnoreCaseAndActiveTrue(String state);

    List<ProcurementCenter> findByActiveTrue();
}