package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.Coordinates;
import com.example.ems_command_center.model.User;
import com.example.ems_command_center.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Drivers", description = "Driver profile and status management")
public class DriverController {

    private final UserService userService;

    public DriverController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver profile")
    public ResponseEntity<User> getDriverProfile(@PathVariable String driverId) {
        return ResponseEntity.ok(userService.getUserById(driverId));
    }

    @PatchMapping("/{driverId}/status")
    @Operation(summary = "Update driver status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable String driverId,
            @RequestBody Map<String, Object> request) {
        String status = (String) request.get("status");
        User updated = userService.updateDriverStatus(driverId, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", updated.getId());
        response.put("status", updated.getStatus());
        response.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        response.put("message", "Status updated successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{driverId}/location")
    @Operation(summary = "Update driver location")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @PathVariable String driverId,
            @RequestBody Map<String, Object> request) {
        double lat = (double) request.get("latitude");
        double lng = (double) request.get("longitude");
        
        userService.updateDriverLocation(driverId, new Coordinates(lat, lng));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Location updated successfully");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        return ResponseEntity.ok(response);
    }
}
