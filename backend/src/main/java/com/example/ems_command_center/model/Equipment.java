package com.example.ems_command_center.model;

public record Equipment(
    String id,
    String name,
    String status, // "functional" | "needs-maintenance" | "out-of-service"
    String lastChecked,
    Integer quantity
) {
}
