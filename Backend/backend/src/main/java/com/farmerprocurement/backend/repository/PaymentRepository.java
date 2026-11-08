package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.Payment;
import com.farmerprocurement.backend.entity.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProcurement_Id(Long procurementId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByProcurement_ProcurementToken_Farmer_User_Id(Long userId);

}
