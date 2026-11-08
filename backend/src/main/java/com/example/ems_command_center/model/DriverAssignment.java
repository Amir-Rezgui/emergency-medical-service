package com.example.ems_command_center.model;

public record DriverAssignment(
    User driver,
    Vehicle assignedVehicle,
    DispatchAssignment currentAssignment
) {}
