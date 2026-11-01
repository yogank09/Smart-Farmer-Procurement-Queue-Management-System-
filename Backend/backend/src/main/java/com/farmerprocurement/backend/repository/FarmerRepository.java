package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Optional<Farmer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByFarmerId(String farmerId);
}