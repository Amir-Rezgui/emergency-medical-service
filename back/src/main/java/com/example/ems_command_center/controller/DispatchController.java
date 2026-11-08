package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.AmbulanceRouteResponse;
import com.example.ems_command_center.model.DispatchAssignmentResponse;
import com.example.ems_command_center.model.DispatchRequest;
import com.example.ems_command_center.model.Vehicle;
import com.example.ems_command_center.model.AssignmentState;
import com.example.ems_command_center.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch")
@Tag(name = "Dispatch", description = "Ambulance routing and dispatch operations")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping("/ambulances/available")
    @Operation(summary = "List all available ambulances")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    public ResponseEntity<List<Vehicle>> getAvailableAmbulances() {
        return ResponseEntity.ok(dispatchService.getAvailableAmbulances());
    }

    @GetMapping("/routes")
    @Operation(summary = "Preview the suggested route from an ambulance to an incident")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    public ResponseEntity<AmbulanceRouteResponse> getRoute(
            @RequestParam String vehicleId,
            @RequestParam String incidentId) {
        return ResponseEntity.ok(dispatchService.getRoute(vehicleId, incidentId));
    }

    @GetMapping("/assignments/{id}")
    @Operation(summary = "Get a saved dispatch assignment by ID")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.canAccessDispatchAssignment(authentication, #id)")
    public ResponseEntity<DispatchAssignmentResponse> getAssignmentById(@PathVariable String id) {
        return ResponseEntity.ok(dispatchService.getAssignmentById(id));
    }

    @GetMapping("/assignments/hospital/{hospitalId}")
    @Operation(summary = "List saved dispatch assignments for a hospital")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @accessControlService.isAssignedHospital(authentication, #hospitalId))")
    public ResponseEntity<List<DispatchAssignmentResponse>> getAssignmentsByHospital(@PathVariable String hospitalId) {
        return ResponseEntity.ok(dispatchService.getAssignmentsByHospital(hospitalId));
    }

    @GetMapping("/assignments/ambulance/{vehicleId}")
    @Operation(summary = "List saved dispatch assignments for an ambulance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    public ResponseEntity<List<DispatchAssignmentResponse>> getAssignmentsByVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(dispatchService.getAssignmentsByVehicle(vehicleId));
    }

    @PostMapping("/assignments")
    @Operation(summary = "Dispatch an ambulance to an incident")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DispatchAssignmentResponse> dispatchAmbulance(@RequestBody DispatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dispatchService.dispatchAmbulance(request));
    }

    @PutMapping("/assignments/{id}/state/{state}")
    @Operation(summary = "Update a dispatch assignment state")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    public ResponseEntity<Void> updateAssignmentState(
            @PathVariable String id,
            @PathVariable AssignmentState state) {
        dispatchService.updateAssignmentState(id, state);
        return ResponseEntity.ok().build();
    }
}
