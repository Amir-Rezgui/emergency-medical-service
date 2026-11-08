package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.Facility;
import com.example.ems_command_center.service.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@Tag(name = "Hospitals", description = "Hospital-specific CRUD operations")
public class HospitalController {

    private final FacilityService facilityService;

    public HospitalController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    @Operation(summary = "Fetch all hospitals with ICU and wait-time details")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER', 'USER')")
    public ResponseEntity<List<Facility>> getAllHospitals() {
        return ResponseEntity.ok(facilityService.getAllHospitals());
    }

    @PostMapping
    @Operation(summary = "Add a new hospital")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Facility> addHospital(@RequestBody Facility hospital) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facilityService.createHospital(hospital));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update hospital details or status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Facility> updateHospital(@PathVariable String id, @RequestBody Facility updates) {
        return facilityService.updateHospital(id, updates)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a hospital")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHospital(@PathVariable String id) {
        return facilityService.deleteHospital(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
