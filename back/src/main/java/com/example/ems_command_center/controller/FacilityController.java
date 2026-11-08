package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.Facility;
import com.example.ems_command_center.service.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@Tag(name = "Facilities", description = "Emergency facilities overview")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    @Operation(summary = "Fetch all emergency facilities")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DRIVER', 'USER')")
    public ResponseEntity<List<Facility>> getAllFacilities() {
        return ResponseEntity.ok(facilityService.getAllFacilities());
    }
}
