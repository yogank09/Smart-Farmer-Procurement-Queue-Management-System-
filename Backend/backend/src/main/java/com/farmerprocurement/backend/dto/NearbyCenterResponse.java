package com.farmerprocurement.backend.dto;

public record NearbyCenterResponse(
        ProcurementCenterResponse center,
        double distanceKm) {
}
