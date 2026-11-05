package com.farmerprocurement.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "procurement_queue",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_queue_token",
                        columnNames = "token_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_queue_slot_status",
                        columnList = "slot_id, status"
                )
        }
)
public class ProcurementQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Farmer's procurement token
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "token_id",
            nullable = false,
            unique = true
    )
    private ProcurementToken procurementToken;

    // Procurement slot associated with this queue entry
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "slot_id",
            nullable = false
    )
    private ProcurementSlot procurementSlot;

    // Position in the queue
    @Column(nullable = false)
    private Integer queuePosition;

    // Current queue status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QueueStatus status = QueueStatus.WAITING;

    // Estimated waiting time in minutes
    @Column(nullable = false)
    private Integer estimatedWaitingTime = 0;

    // Time when farmer entered the queue
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    // Time when farmer's procurement started
    private LocalDateTime processingStartedAt;

    // Time when farmer's procurement completed
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProcurementToken getProcurementToken() {
        return procurementToken;
    }

    public void setProcurementToken(ProcurementToken procurementToken) {
        this.procurementToken = procurementToken;
    }

    public ProcurementSlot getProcurementSlot() {
        return procurementSlot;
    }

    public void setProcurementSlot(ProcurementSlot procurementSlot) {
        this.procurementSlot = procurementSlot;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public void setStatus(QueueStatus status) {
        this.status = status;
    }

    public Integer getEstimatedWaitingTime() {
        return estimatedWaitingTime;
    }

    public void setEstimatedWaitingTime(Integer estimatedWaitingTime) {
        this.estimatedWaitingTime = estimatedWaitingTime;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void setProcessingStartedAt(
            LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

