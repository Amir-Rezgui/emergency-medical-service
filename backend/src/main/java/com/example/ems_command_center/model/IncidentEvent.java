package com.example.ems_command_center.model;

public record IncidentEvent(
    String action,
    String incidentId,
    Incident incident
) {
}
