package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bed_availability")
public record BedAvailability(
    @Id String id,
    String ward,
    Integer totalBeds,
    Integer occupiedBeds,
    Integer availableBeds,
    Integer reservedBeds,
    String status
) {
}
