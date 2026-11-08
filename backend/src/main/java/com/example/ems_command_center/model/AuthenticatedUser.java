package com.example.ems_command_center.model;

import java.util.List;

public record AuthenticatedUser(
    String username,
    String subject,
    String email,
    String firstName,
    String lastName,
    List<String> roles,
    String hospitalId,     // JWT claim: hospital_id (MANAGER scope)
    String ambulanceId    // JWT claim: ambulance_id (DRIVER scope)
) {
}
