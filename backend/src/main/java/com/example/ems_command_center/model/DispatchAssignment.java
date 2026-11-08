package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "dispatch_assignments")
public record DispatchAssignment(
    @Id String id,
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
    LocalDateTime createdAt,
    List<String> incidentTags,
    AmbulanceRouteResponse route
) {
}
