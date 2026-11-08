package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reports")
public record Report(
    @Id String id,
    String name,
    String category,
    String date,
    String status // "Ready" | "Archived"
) {
}
