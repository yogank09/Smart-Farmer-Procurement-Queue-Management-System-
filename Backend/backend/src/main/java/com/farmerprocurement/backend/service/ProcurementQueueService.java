package com.farmerprocurement.backend.service;

import com.farmerprocurement.backend.dto.ProcurementQueueRequest;
import com.farmerprocurement.backend.dto.ProcurementQueueResponse;
import com.farmerprocurement.backend.entity.ProcurementQueue;
import com.farmerprocurement.backend.entity.ProcurementToken;
import com.farmerprocurement.backend.entity.ProcurementSlot;
import com.farmerprocurement.backend.entity.QueueStatus;
import com.farmerprocurement.backend.entity.TokenStatus;
import com.farmerprocurement.backend.repository.ProcurementQueueRepository;
import com.farmerprocurement.backend.repository.ProcurementTokenRepository;
import com.farmerprocurement.backend.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcurementQueueService {

    private static final int ESTIMATED_MINUTES_PER_FARMER = 30;

    private final ProcurementQueueRepository queueRepository;
    private final ProcurementTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public ProcurementQueueService(
            ProcurementQueueRepository queueRepository,
            ProcurementTokenRepository tokenRepository,
            UserRepository userRepository) {

        this.queueRepository = queueRepository;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    // Join Queue
    @Transactional
    public ProcurementQueueResponse joinQueue(
            ProcurementQueueRequest request,
            UserDetails userDetails) {

        Long userId = getUserId(userDetails);

        ProcurementToken token = tokenRepository
                .findByIdAndFarmerUserId(request.getTokenId(), userId)
                .orElseThrow(() -> new RuntimeException(
                        "Procurement token not found or does not belong to you"));

        if (token.getStatus() != TokenStatus.BOOKED
                && token.getStatus() != TokenStatus.WAITING
                && token.getStatus() != TokenStatus.IN_QUEUE) {

            throw new RuntimeException(
                    "Only active procurement tokens can join the queue");
        }

        if (queueRepository.existsByProcurementTokenId(token.getId())) {
            throw new RuntimeException(
                    "This procurement token has already joined the queue");
        }

        ProcurementSlot slot = token.getProcurementSlot();

        List<ProcurementQueue> existingQueues =
                queueRepository.findByProcurementSlotIdOrderByQueuePositionAsc(
                        slot.getId());

        int nextPosition = 1;

        for (ProcurementQueue queue : existingQueues) {

            if (queue.getStatus() != QueueStatus.CANCELLED
                    && queue.getStatus() != QueueStatus.COMPLETED) {

                nextPosition = Math.max(
                        nextPosition,
                        queue.getQueuePosition() + 1);
            }
        }

        ProcurementQueue queue = new ProcurementQueue();

        queue.setProcurementToken(token);
        queue.setProcurementSlot(slot);
        queue.setQueuePosition(nextPosition);
        queue.setStatus(QueueStatus.WAITING);

        int estimatedWaitingTime =
                (nextPosition - 1) * ESTIMATED_MINUTES_PER_FARMER;

        queue.setEstimatedWaitingTime(estimatedWaitingTime);

        token.setStatus(TokenStatus.IN_QUEUE);

        ProcurementQueue savedQueue = queueRepository.save(queue);

        return mapToResponse(savedQueue);
    }

    // Get My Queue
    @Transactional(readOnly = true)
    public List<ProcurementQueueResponse> getMyQueue(
            UserDetails userDetails) {

        Long userId = getUserId(userDetails);

        List<ProcurementQueue> queues =
                queueRepository.findByProcurementTokenFarmerUserId(userId);

        List<ProcurementQueueResponse> responses = new ArrayList<>();

        for (ProcurementQueue queue : queues) {
            responses.add(mapToResponse(queue));
        }

        return responses;
    }

    // Get Queue By ID
    @Transactional(readOnly = true)
    public ProcurementQueueResponse getQueueById(
            Long queueId,
            UserDetails userDetails) {

        Long userId = getUserId(userDetails);

        ProcurementQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException(
                        "Queue entry not found"));

        if (!queue.getProcurementToken()
                .getFarmer()
                .getUser()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to access this queue entry");
        }

        return mapToResponse(queue);
    }

    // Get Queue By Slot
    @Transactional(readOnly = true)
    public List<ProcurementQueueResponse> getQueueBySlot(Long slotId) {

        List<ProcurementQueue> queues =
                queueRepository.findByProcurementSlotIdOrderByQueuePositionAsc(
                        slotId);

        List<ProcurementQueueResponse> responses = new ArrayList<>();

        for (ProcurementQueue queue : queues) {
            responses.add(mapToResponse(queue));
        }

        return responses;
    }

    // Call Next Farmer
    @Transactional
    public ProcurementQueueResponse callNextFarmer(Long slotId) {

        List<ProcurementQueue> waitingQueues = queueRepository
                .findByProcurementSlotIdAndStatusOrderByQueuePositionAsc(
                        slotId,
                        QueueStatus.WAITING);

        if (waitingQueues.isEmpty()) {
            throw new RuntimeException(
                    "No waiting farmers available in this slot");
        }

        ProcurementQueue nextQueue = waitingQueues.get(0);

        nextQueue.setStatus(QueueStatus.CALLED);

        ProcurementToken token = nextQueue.getProcurementToken();
        token.setStatus(TokenStatus.IN_QUEUE);

        ProcurementQueue savedQueue = queueRepository.save(nextQueue);

        return mapToResponse(savedQueue);
    }

    // Start Processing
    @Transactional
    public ProcurementQueueResponse startProcessing(Long queueId) {

        ProcurementQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException(
                        "Queue entry not found"));

        if (queue.getStatus() != QueueStatus.CALLED) {
            throw new RuntimeException(
                    "Only a called farmer can start processing");
        }

        queue.setStatus(QueueStatus.PROCESSING);
        queue.setProcessingStartedAt(LocalDateTime.now());

        ProcurementToken token = queue.getProcurementToken();
        token.setStatus(TokenStatus.PROCESSING);

        ProcurementQueue savedQueue = queueRepository.save(queue);

        return mapToResponse(savedQueue);
    }

    // Complete Procurement
    @Transactional
    public ProcurementQueueResponse completeProcurement(Long queueId) {

        ProcurementQueue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException(
                        "Queue entry not found"));

        if (queue.getStatus() != QueueStatus.PROCESSING) {
            throw new RuntimeException(
                    "Farmer must be in PROCESSING status before completion");
        }

        queue.setStatus(QueueStatus.COMPLETED);
        queue.setCompletedAt(LocalDateTime.now());

        ProcurementToken token = queue.getProcurementToken();
        token.setStatus(TokenStatus.COMPLETED);

        ProcurementQueue savedQueue = queueRepository.save(queue);

        return mapToResponse(savedQueue);
    }

    // Get User ID
    private Long getUserId(UserDetails userDetails) {

        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(
                        "User not found"))
                .getId();
    }

    // Map Queue Entity To Response
    private ProcurementQueueResponse mapToResponse(
            ProcurementQueue queue) {

        ProcurementQueueResponse response =
                new ProcurementQueueResponse();

        ProcurementToken token = queue.getProcurementToken();
        ProcurementSlot slot = queue.getProcurementSlot();

        response.setId(queue.getId());
        response.setTokenId(token.getId());
        response.setTokenNumber(token.getTokenNumber());
        response.setFarmerId(token.getFarmer().getId());
        response.setSlotId(slot.getId());
        response.setCenterId(slot.getProcurementCenter().getId());
        response.setCenterName(
                slot.getProcurementCenter().getCenterName());
        response.setCropName(slot.getCropName());
        response.setQueuePosition(queue.getQueuePosition());
        response.setStatus(queue.getStatus());
        response.setEstimatedWaitingTime(
                queue.getEstimatedWaitingTime());
        response.setJoinedAt(queue.getJoinedAt());
        response.setProcessingStartedAt(
                queue.getProcessingStartedAt());
        response.setCompletedAt(queue.getCompletedAt());

        return response;
    }
}
