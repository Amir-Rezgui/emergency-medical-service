package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "analytics")
public record Analytics(
    @Id String id,
    String type,
    List<Map<String, Object>> data
) {
}
