package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "incidents")
public record Incident(
    @Id String id,
    String title,
    String location,
    Coordinates coordinates,
    String time,
    @DBRef
    User reporter,      
    String type, // "urgent" | "normal"
    List<String> tags,
    String status,
    int priority
) {
    // Custom withers or constructor logic can go here if needed
}
