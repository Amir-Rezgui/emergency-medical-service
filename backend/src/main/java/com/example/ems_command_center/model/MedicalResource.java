package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "medical_resources")
public record MedicalResource(
    @Id String id,
    String name,
    String category,
    Integer availableUnits,
    Integer totalUnits,
    String status,
    String location,
    String lastUpdated
) {
}
