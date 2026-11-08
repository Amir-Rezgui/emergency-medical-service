package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.Incident;
import com.example.ems_command_center.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@Tag(name = "Incidents", description = "Emergency incident management")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @Operation(summary = "Fetch all incidents sorted by priority")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER', 'DRIVER')")
    public ResponseEntity<List<Incident>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/by-id/{id}")
    @Operation(summary = "Fetch a single incident by id")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER', 'DRIVER')")
    public ResponseEntity<Incident> getIncidentById(@PathVariable String id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @PostMapping
    @Operation(summary = "Report a new incident")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER', 'DRIVER')")
    public ResponseEntity<Incident> createIncident(@RequestBody Incident incident) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.createIncident(incident));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing incident")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Incident> updateIncident(@PathVariable String id, @RequestBody Incident incident) {
        return ResponseEntity.ok(incidentService.updateIncident(id, incident));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an incident")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteIncident(@PathVariable String id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }
}
