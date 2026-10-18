package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.entity.PendingRegistration;
import com.farmerprocurement.backend.repository.PendingRegistrationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpAttemptService {

    private final PendingRegistrationRepository pendingRegistrationRepository;

    public OtpAttemptService(
            PendingRegistrationRepository pendingRegistrationRepository) {
        this.pendingRegistrationRepository = pendingRegistrationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(Long pendingId) {

        PendingRegistration pending = pendingRegistrationRepository
                .findById(pendingId)
                .orElseThrow(() ->
                        new IllegalStateException("Pending registration not found"));

        int attempts = pending.getFailedAttempts() + 1;
        pending.setFailedAttempts(attempts);

        pendingRegistrationRepository.saveAndFlush(pending);

        return attempts;
    }
}

