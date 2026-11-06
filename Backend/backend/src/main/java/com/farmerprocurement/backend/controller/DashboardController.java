package com.farmerprocurement.backend.controller;

import com.farmerprocurement.backend.dto.DashboardSummaryResponse;
import com.farmerprocurement.backend.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {

        DashboardSummaryResponse response =
                dashboardService.getDashboardSummary();

        return ResponseEntity.ok(response);
    }
}