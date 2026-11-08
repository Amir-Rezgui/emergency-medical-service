package com.example.ems_command_center.model;

import java.util.List;

public record DispatchAssignmentResponse(
    String assignmentId,
    String incidentId,
    String incidentTitle,
    String vehicleId,
    String vehicleName,
    String driverId,
    String driverName,
    String hospitalId,
    String hospitalName,
    String dispatcher,
    String notes,
    String vehicleStatus,
    AssignmentState state,
    String dispatchedAt,
    List<String> incidentTags,
    AmbulanceRouteResponse route
) {
}
