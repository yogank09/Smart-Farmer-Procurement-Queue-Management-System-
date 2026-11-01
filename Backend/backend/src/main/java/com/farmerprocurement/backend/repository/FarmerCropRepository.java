package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.FarmerCrop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerCropRepository
        extends JpaRepository<FarmerCrop, Long> {

    List<FarmerCrop> findByFarmerId(Long farmerId);

    Optional<FarmerCrop> findByIdAndFarmerId(
            Long id,
            Long farmerId
    );
}