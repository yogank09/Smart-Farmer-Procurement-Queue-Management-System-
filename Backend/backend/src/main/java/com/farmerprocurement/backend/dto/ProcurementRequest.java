package com.farmerprocurement.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ProcurementRequest {

    @NotNull(message = "Token ID is required")
    @Positive(message = "Token ID must be positive")
    private Long tokenId;

    @NotNull(message = "Actual quantity is required")
    @DecimalMin(value = "0.01", message = "Actual quantity must be greater than zero")
    private BigDecimal actualQuantity;

    @NotBlank(message = "Quality grade is required")
    private String qualityGrade;

    @NotNull(message = "Rate per quintal is required")
    @DecimalMin(value = "0.01", message = "Rate per quintal must be greater than zero")
    private BigDecimal ratePerQuintal;

    public ProcurementRequest() {
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
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

}
