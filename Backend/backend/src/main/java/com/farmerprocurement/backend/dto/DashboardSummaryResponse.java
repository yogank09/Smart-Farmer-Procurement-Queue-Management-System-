package com.farmerprocurement.backend.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private long totalFarmers;

    private long totalProcurements;

    private long completedProcurements;

    private BigDecimal totalProcurementAmount;

    private long totalPayments;

    private long completedPayments;

    private BigDecimal totalPaymentAmount;

    public DashboardSummaryResponse() {
    }

    public long getTotalFarmers() {
        return totalFarmers;
    }

    public void setTotalFarmers(long totalFarmers) {
        this.totalFarmers = totalFarmers;
    }

    public long getTotalProcurements() {
        return totalProcurements;
    }

    public void setTotalProcurements(long totalProcurements) {
        this.totalProcurements = totalProcurements;
    }

    public long getCompletedProcurements() {
        return completedProcurements;
    }

    public void setCompletedProcurements(long completedProcurements) {
        this.completedProcurements = completedProcurements;
    }

    public BigDecimal getTotalProcurementAmount() {
        return totalProcurementAmount;
    }

    public void setTotalProcurementAmount(BigDecimal totalProcurementAmount) {
        this.totalProcurementAmount = totalProcurementAmount;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public long getCompletedPayments() {
        return completedPayments;
    }

    public void setCompletedPayments(long completedPayments) {
        this.completedPayments = completedPayments;
    }

    public BigDecimal getTotalPaymentAmount() {
        return totalPaymentAmount;
    }

    public void setTotalPaymentAmount(BigDecimal totalPaymentAmount) {
        this.totalPaymentAmount = totalPaymentAmount;
    }
}