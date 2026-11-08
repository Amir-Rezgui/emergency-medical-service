package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.DashboardStats;
import com.example.ems_command_center.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistics", description = "Real-time dashboard statistics")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    @Operation(summary = "Fetch real-time dashboard statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER', 'USER')")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(statsService.getDashboardStats());
    }
}
