package com.example.ems_command_center.model;

public record DispatchRequest(
    String incidentId,
    String vehicleId,
    String hospitalId,
    String dispatcher,
    String notes
) {
}
