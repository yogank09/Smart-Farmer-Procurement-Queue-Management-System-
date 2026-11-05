package com.farmerprocurement.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurements", uniqueConstraints = {
        @UniqueConstraint(name = "uk_procurement_token", columnNames = "token_id")
})
public class Procurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token associated with this procurement
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "token_id", nullable = false, unique = true)
    private ProcurementToken procurementToken;

    // Actual crop quantity received in KG
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal actualQuantity;

    // Crop quality grade, e.g. A, B, C
    @Column(nullable = false, length = 20)
    private String qualityGrade;

    // Procurement rate per quintal
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerQuintal;

    // Total procurement amount
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProcurementStatus status = ProcurementStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    public Procurement() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ProcurementToken getProcurementToken() {
        return procurementToken;
    }

    public void setProcurementToken(ProcurementToken procurementToken) {
        this.procurementToken = procurementToken;
    }

    public BigDecimal getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(BigDecimal actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public BigDecimal getRatePerQuintal() {
        return ratePerQuintal;
    }

    public void setRatePerQuintal(BigDecimal ratePerQuintal) {
        this.ratePerQuintal = ratePerQuintal;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ProcurementStatus getStatus() {
        return status;
    }

    public void setStatus(ProcurementStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

}
