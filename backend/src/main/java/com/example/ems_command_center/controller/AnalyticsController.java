package com.example.ems_command_center.controller;

import com.example.ems_command_center.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Dispatch and response time analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dispatch")
    @Operation(summary = "Aggregated dispatch volume over time")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getDispatchVolume() {
        return ResponseEntity.ok(analyticsService.getDispatchVolume());
    }

    @GetMapping("/response")
    @Operation(summary = "Response time data by day")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getResponseTimeData() {
        return ResponseEntity.ok(analyticsService.getResponseTimeData());
    }
}
