package com.farmerprocurement.backend.dto;

import java.util.List;

public record NearbyCentersResponse(
        String locationLabel,
        List<NearbyCenterResponse> centers,
        String attribution,
        String providerNotice) {
}
