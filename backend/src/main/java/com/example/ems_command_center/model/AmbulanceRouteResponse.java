package com.example.ems_command_center.model;

import java.util.List;

public record AmbulanceRouteResponse(
    String vehicleId,
    String vehicleName,
    String incidentId,
    String incidentTitle,
    Coordinates origin,
    Coordinates destination,
    List<Coordinates> path,
    double distanceKm,
    int estimatedMinutes,
    String trafficLevel,
    List<String> turnByTurn
) {
}
