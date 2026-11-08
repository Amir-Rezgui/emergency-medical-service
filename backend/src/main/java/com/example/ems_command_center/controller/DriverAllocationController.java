package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.User;
import com.example.ems_command_center.model.Vehicle;
import com.example.ems_command_center.service.DriverAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/driver-allocation")
@Tag(name = "Admin - Driver Allocation", description = "Admin-only endpoints for allocating drivers to ambulances")
public class DriverAllocationController {

    private final DriverAllocationService driverAllocationService;

    public DriverAllocationController(DriverAllocationService driverAllocationService) {
        this.driverAllocationService = driverAllocationService;
    }

    /**
     * Allocate a driver to an ambulance (Admin only)
     * @param driverId ID of the driver
     * @param ambulanceId ID of the ambulance
     * @return Response with allocation details
     */
    @PostMapping("/allocate/{driverId}/{ambulanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Allocate driver to ambulance (Admin only)")
    public ResponseEntity<Map<String, Object>> allocateDriver(
            @PathVariable String driverId,
            @PathVariable String ambulanceId) {
        Map<String, Object> result = driverAllocationService.allocateDriverToAmbulance(driverId, ambulanceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Deallocate a driver from an ambulance (Admin only)
     * @param driverId ID of the driver
     * @param ambulanceId ID of the ambulance
     * @return Response with deallocation details
     */
    @PostMapping("/deallocate/{driverId}/{ambulanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deallocate driver from ambulance (Admin only)")
    public ResponseEntity<Map<String, Object>> deallocateDriver(
            @PathVariable String driverId,
            @PathVariable String ambulanceId) {
        Map<String, Object> result = driverAllocationService.deallocateDriverFromAmbulance(driverId, ambulanceId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all drivers allocated to a specific ambulance (Admin only)
     * @param ambulanceId ID of the ambulance
     * @return List of drivers
     */
    @GetMapping("/ambulance/{ambulanceId}/drivers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all drivers assigned to an ambulance (Admin only)")
    public ResponseEntity<List<User>> getAmbulanceDrivers(@PathVariable String ambulanceId) {
        List<User> drivers = driverAllocationService.getAmbulanceDrivers(ambulanceId);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get the ambulance allocated to a specific driver (Admin only)
     * @param driverId ID of the driver
     * @return Ambulance details
     */
    @GetMapping("/driver/{driverId}/ambulance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get ambulance assigned to a driver (Admin only)")
    public ResponseEntity<Vehicle> getDriverAmbulance(@PathVariable String driverId) {
        Vehicle ambulance = driverAllocationService.getDriverAmbulance(driverId);
        return ResponseEntity.ok(ambulance);
    }

    /**
     * Get allocation status for a specific driver (Admin only)
     * @param driverId ID of the driver
     * @return Allocation status details
     */
    @GetMapping("/driver/{driverId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get driver allocation status (Admin only)")
    public ResponseEntity<Map<String, Object>> getDriverAllocationStatus(@PathVariable String driverId) {
        Map<String, Object> status = driverAllocationService.getDriverAllocationStatus(driverId);
        return ResponseEntity.ok(status);
    }
}
