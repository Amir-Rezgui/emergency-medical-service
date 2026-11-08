package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "vehicles")
public record Vehicle(
    @Id String id,
    String name,
    String status, // "available" | "busy" | "maintenance" | "out-of-service"
    String type,   // "ambulance" | "supervisor"
    Coordinates location,
    String driverId,
    List<String> crew,
    String lastUpdate,
    List<Equipment> equipment
) {
    public Vehicle(
            String id,
            String name,
            String status,
            String type,
            Coordinates location,
            String driverId,
            List<String> crew,
            String lastUpdate,
            List<Equipment> equipment) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.type = type;
        this.location = location;
        this.driverId = driverId;
        this.crew = crew;
        this.lastUpdate = lastUpdate;
        this.equipment = equipment;
    }
}
