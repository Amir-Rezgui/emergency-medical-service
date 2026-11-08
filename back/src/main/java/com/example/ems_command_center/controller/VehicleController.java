package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.Vehicle;
import com.example.ems_command_center.model.User;
import com.example.ems_command_center.service.DriverAllocationService;
import com.example.ems_command_center.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Fleet and vehicle management")
public class VehicleController {

    private final VehicleService vehicleService;
    private final DriverAllocationService driverAllocationService;

    public VehicleController(VehicleService vehicleService, DriverAllocationService driverAllocationService) {
        this.vehicleService = vehicleService;
        this.driverAllocationService = driverAllocationService;
    }

    @GetMapping
    @Operation(summary = "Fetch all vehicles (ambulances, supervisors, etc.)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER')")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @PostMapping
    @Operation(summary = "Register a new vehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(vehicle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle status or location")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (hasRole('DRIVER') and @accessControlService.isAssignedAmbulance(authentication, #id))")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable String id, @RequestBody Vehicle updates) {
        return vehicleService.updateVehicle(id, updates)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("availability/{id}")
    @Operation(summary = "Update vehicle availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (hasRole('DRIVER') and @accessControlService.isAssignedAmbulance(authentication, #id))")
    public ResponseEntity<Vehicle> updateVehicleAvailability(@PathVariable String id) {
        return vehicleService.updateVehicleAvailability(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Decommission a vehicle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable String id) {
        return vehicleService.deleteVehicle(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("driver/{id}")
    @Operation(summary = "Update vehicle driver")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> updateVehicleDriver(@PathVariable String id,
            @RequestBody String driverId) {
        Map<String, Object> result = driverAllocationService.allocateDriverToAmbulance(driverId, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
