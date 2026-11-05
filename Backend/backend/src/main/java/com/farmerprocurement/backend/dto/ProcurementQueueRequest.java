package com.farmerprocurement.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProcurementQueueRequest {

    @NotNull(message = "Token ID is required")
    @Positive(message = "Token ID must be greater than zero")
    private Long tokenId;

    public ProcurementQueueRequest() {
    }

    public ProcurementQueueRequest(Long tokenId) {
        this.tokenId = tokenId;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }
}