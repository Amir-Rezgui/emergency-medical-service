package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "facilities")
public record Facility(
    @Id String id,
    String name,
    String status,
    String beds,
    String distance,
    String type, // "error" | "warning" | "success"
    int occupancy,
    Coordinates coordinates,
    List<Equipment> equipment,

    // Hospital-specific fields
    String facilityType,   // e.g., "Level 1 Trauma", "Specialist Hub"
    String waitTime,
    int icu,
    int icuTotal,
    String waitType
) {
}
