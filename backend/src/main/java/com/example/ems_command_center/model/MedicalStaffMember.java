package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "medical_staff")
public record MedicalStaffMember(
    @Id String id,
    String name,
    String role,
    String specialty,
    String shift,
    String status
) {
}
