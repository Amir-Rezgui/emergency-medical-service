package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.BedAvailability;
import com.example.ems_command_center.model.HospitalManagerOverview;
import com.example.ems_command_center.model.HospitalPatient;
import com.example.ems_command_center.model.MedicalResource;
import com.example.ems_command_center.service.HospitalManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hospital-manager")
@Tag(name = "Hospital Manager", description = "Hospital care coordination and patient validation")
public class HospitalManagerController {

    private final HospitalManagerService hospitalManagerService;

    public HospitalManagerController(HospitalManagerService hospitalManagerService) {
        this.hospitalManagerService = hospitalManagerService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Fetch emergencies, patient dossiers, staff, beds, and medical resources")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<HospitalManagerOverview> getOverview() {
        return ResponseEntity.ok(hospitalManagerService.getOverview());
    }

    @PutMapping("/patients/{id}")
    @Operation(summary = "Update patient state or dossier details")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<HospitalPatient> updatePatient(@PathVariable String id, @RequestBody HospitalPatient updates) {
        return ResponseEntity.ok(hospitalManagerService.updatePatient(id, updates));
    }

    @PutMapping("/beds/{id}")
    @Operation(summary = "Update bed availability in a ward")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BedAvailability> updateBed(@PathVariable String id, @RequestBody BedAvailability updates) {
        return ResponseEntity.ok(hospitalManagerService.updateBed(id, updates));
    }

    @PutMapping("/resources/{id}")
    @Operation(summary = "Update the availability of a medical resource")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MedicalResource> updateResource(@PathVariable String id, @RequestBody MedicalResource updates) {
        return ResponseEntity.ok(hospitalManagerService.updateResource(id, updates));
    }

    @PostMapping("/patients/{id}/validate-care")
    @Operation(summary = "Validate that the patient has been taken in charge")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<HospitalPatient> validateCare(@PathVariable String id, @RequestBody(required = false) Map<String, String> payload) {
        String validator = payload != null ? payload.get("validator") : null;
        return ResponseEntity.ok(hospitalManagerService.validateCare(id, validator));
    }
}
