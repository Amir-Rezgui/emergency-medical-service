package com.example.ems_command_center.model;

/**
 * Response DTO for /api/stats — not a MongoDB document.
 */
public record DashboardStats(
    int activeAmbulances,
    int totalAmbulances,
    String avgResponseTime,
    int hospitalOccupancy,
    int criticalIncidents
) {
}
