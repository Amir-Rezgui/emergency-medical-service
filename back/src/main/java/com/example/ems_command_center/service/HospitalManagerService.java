package com.example.ems_command_center.service;

import com.example.ems_command_center.model.BedAvailability;
import com.example.ems_command_center.model.HospitalManagerOverview;
import com.example.ems_command_center.model.HospitalPatient;
import com.example.ems_command_center.model.MedicalResource;
import com.example.ems_command_center.repository.BedAvailabilityRepository;
import com.example.ems_command_center.repository.HospitalPatientRepository;
import com.example.ems_command_center.repository.MedicalResourceRepository;
import com.example.ems_command_center.repository.MedicalStaffRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class HospitalManagerService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final IncidentService incidentService;
    private final HospitalPatientRepository patientRepository;
    private final BedAvailabilityRepository bedRepository;
    private final MedicalResourceRepository resourceRepository;
    private final MedicalStaffRepository staffRepository;

    public HospitalManagerService(
        IncidentService incidentService,
        HospitalPatientRepository patientRepository,
        BedAvailabilityRepository bedRepository,
        MedicalResourceRepository resourceRepository,
        MedicalStaffRepository staffRepository
    ) {
        this.incidentService = incidentService;
        this.patientRepository = patientRepository;
        this.bedRepository = bedRepository;
        this.resourceRepository = resourceRepository;
        this.staffRepository = staffRepository;
    }

    public HospitalManagerOverview getOverview() {
        return new HospitalManagerOverview(
            incidentService.getAllIncidents(),
            patientRepository.findAll(),
            bedRepository.findAll(),
            resourceRepository.findAll(),
            staffRepository.findAll()
        );
    }

    public HospitalPatient updatePatient(String id, HospitalPatient updates) {
        HospitalPatient existing = patientRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        HospitalPatient updated = new HospitalPatient(
            existing.id(),
            firstNonBlank(updates.patientCode(), existing.patientCode()),
            firstNonBlank(updates.patientName(), existing.patientName()),
            updates.age() > 0 ? updates.age() : existing.age(),
            firstNonBlank(updates.emergencyId(), existing.emergencyId()),
            firstNonBlank(updates.emergencyTitle(), existing.emergencyTitle()),
            firstNonBlank(updates.triageLevel(), existing.triageLevel()),
            firstNonBlank(updates.status(), existing.status()),
            firstNonBlank(updates.assignedDoctor(), existing.assignedDoctor()),
            firstNonBlank(updates.assignedNurse(), existing.assignedNurse()),
            firstNonBlank(updates.room(), existing.room()),
            firstNonBlank(updates.dossierSummary(), existing.dossierSummary()),
            updates.careSteps() != null && !updates.careSteps().isEmpty() ? updates.careSteps() : existing.careSteps(),
            updates.careValidated() || existing.careValidated(),
            firstNonBlank(updates.validatedBy(), existing.validatedBy()),
            now()
        );

        return patientRepository.save(updated);
    }

    public BedAvailability updateBed(String id, BedAvailability updates) {
        BedAvailability existing = bedRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bed status not found"));

        int totalBeds = updates.totalBeds() != null && updates.totalBeds() > 0 ? updates.totalBeds() : existing.totalBeds();
        int occupiedBeds = updates.occupiedBeds() != null && updates.occupiedBeds() >= 0 ? updates.occupiedBeds() : existing.occupiedBeds();
        int reservedBeds = updates.reservedBeds() != null && updates.reservedBeds() >= 0 ? updates.reservedBeds() : existing.reservedBeds();
        int calculatedAvailableBeds = Math.max(totalBeds - occupiedBeds - reservedBeds, 0);

        BedAvailability updated = new BedAvailability(
            existing.id(),
            firstNonBlank(updates.ward(), existing.ward()),
            totalBeds,
            occupiedBeds,
            updates.availableBeds() != null && updates.availableBeds() >= 0 ? updates.availableBeds() : calculatedAvailableBeds,
            reservedBeds,
            firstNonBlank(updates.status(), deriveBedStatus(totalBeds, occupiedBeds))
        );

        return bedRepository.save(updated);
    }

    public MedicalResource updateResource(String id, MedicalResource updates) {
        MedicalResource existing = resourceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        int totalUnits = updates.totalUnits() != null && updates.totalUnits() > 0 ? updates.totalUnits() : existing.totalUnits();
        int availableUnits = updates.availableUnits() != null && updates.availableUnits() >= 0 ? updates.availableUnits() : existing.availableUnits();

        MedicalResource updated = new MedicalResource(
            existing.id(),
            firstNonBlank(updates.name(), existing.name()),
            firstNonBlank(updates.category(), existing.category()),
            availableUnits,
            totalUnits,
            firstNonBlank(updates.status(), deriveResourceStatus(availableUnits, totalUnits)),
            firstNonBlank(updates.location(), existing.location()),
            now()
        );

        return resourceRepository.save(updated);
    }

    public HospitalPatient validateCare(String id, String validator) {
        HospitalPatient existing = patientRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        HospitalPatient updated = new HospitalPatient(
            existing.id(),
            existing.patientCode(),
            existing.patientName(),
            existing.age(),
            existing.emergencyId(),
            existing.emergencyTitle(),
            existing.triageLevel(),
            "Validated",
            existing.assignedDoctor(),
            existing.assignedNurse(),
            existing.room(),
            existing.dossierSummary(),
            existing.careSteps(),
            true,
            firstNonBlank(validator, existing.validatedBy()),
            now()
        );

        return patientRepository.save(updated);
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred != null && !preferred.isBlank() ? preferred : fallback;
    }

    private String deriveBedStatus(int totalBeds, int occupiedBeds) {
        if (totalBeds <= 0) {
            return "Unavailable";
        }
        double occupancy = (occupiedBeds * 100.0) / totalBeds;
        if (occupancy >= 90) {
            return "Critical";
        }
        if (occupancy >= 75) {
            return "Busy";
        }
        return "Available";
    }

    private String deriveResourceStatus(int availableUnits, int totalUnits) {
        if (totalUnits <= 0 || availableUnits <= 0) {
            return "Critical";
        }
        if ((availableUnits * 100.0) / totalUnits <= 35) {
            return "Low";
        }
        return "Operational";
    }

    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
