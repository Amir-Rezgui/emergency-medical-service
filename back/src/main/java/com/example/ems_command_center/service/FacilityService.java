package com.example.ems_command_center.service;

import com.example.ems_command_center.model.Facility;
import com.example.ems_command_center.repository.FacilityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FacilityService {

    private static final Set<String> ALLOWED_FACILITY_TYPES = Set.of("error", "warning", "success");
    private static final Set<String> ALLOWED_WAIT_TYPES = Set.of("error", "primary", "success", "warning");

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public List<Facility> getAllHospitals() {
        return facilityRepository.findByFacilityTypeNotNull();
    }

    public Facility createHospital(Facility hospital) {
        validateHospital(hospital, null);
        return facilityRepository.save(normalizeHospital(hospital, null));
    }

    public Optional<Facility> updateHospital(String id, Facility updates) {
        return facilityRepository.findById(id).map(existing -> {
            Facility updated = new Facility(
                existing.id(),
                hasText(updates.name()) ? updates.name() : existing.name(),
                hasText(updates.status()) ? updates.status() : existing.status(),
                hasText(updates.beds()) ? updates.beds() : existing.beds(),
                hasText(updates.distance()) ? updates.distance() : existing.distance(),
                hasText(updates.type()) ? updates.type() : existing.type(),
                updates.occupancy() != 0 ? updates.occupancy() : existing.occupancy(),
                updates.coordinates() != null ? updates.coordinates() : existing.coordinates(),
                updates.equipment() != null ? updates.equipment() : existing.equipment(),
                hasText(updates.facilityType()) ? updates.facilityType() : existing.facilityType(),
                hasText(updates.waitTime()) ? updates.waitTime() : existing.waitTime(),
                updates.icu() != 0 ? updates.icu() : existing.icu(),
                updates.icuTotal() != 0 ? updates.icuTotal() : existing.icuTotal(),
                hasText(updates.waitType()) ? updates.waitType() : existing.waitType()
            );

            validateHospital(updated, existing);
            return facilityRepository.save(normalizeHospital(updated, existing));
        });
    }

    public boolean deleteHospital(String id) {
        if (facilityRepository.existsById(id)) {
            facilityRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public double getAverageOccupancy() {
        return facilityRepository.findAll().stream()
                .mapToInt(Facility::occupancy)
                .average()
                .orElse(0);
    }

    private void validateHospital(Facility hospital, Facility existing) {
        if (!hasText(hospital.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital name is required");
        }
        if (!hasText(hospital.facilityType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "facilityType is required for hospitals");
        }
        if (hospital.coordinates() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital coordinates are required");
        }
        if (hospital.occupancy() < 0 || hospital.occupancy() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital occupancy must be between 0 and 100");
        }
        if (hospital.icu() < 0 || hospital.icuTotal() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ICU values cannot be negative");
        }
        if (hospital.icuTotal() > 0 && hospital.icu() > hospital.icuTotal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ICU occupied beds cannot exceed ICU total beds");
        }
        if (hasText(hospital.type()) && !ALLOWED_FACILITY_TYPES.contains(hospital.type().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital availability type must be one of: error, warning, success");
        }
        if (hasText(hospital.waitType()) && !ALLOWED_WAIT_TYPES.contains(hospital.waitType().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital waitType must be one of: error, primary, success, warning");
        }
        if (hasText(hospital.beds()) && !hospital.beds().trim().matches("\\d+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital beds must be numeric text");
        }
        if (existing != null && hasText(hospital.status()) && hospital.status().equalsIgnoreCase(existing.status())
            && hospital.occupancy() == existing.occupancy() && hospital.icu() == existing.icu() && hospital.icuTotal() == existing.icuTotal()) {
            return;
        }
    }

    private Facility normalizeHospital(Facility hospital, Facility existing) {
        int occupancy = hospital.occupancy();
        String type = hasText(hospital.type()) ? hospital.type().toLowerCase() : deriveFacilityType(occupancy);
        String waitType = hasText(hospital.waitType()) ? hospital.waitType().toLowerCase() : deriveWaitType(occupancy);
        String status = hasText(hospital.status()) ? hospital.status() : deriveStatusLabel(occupancy);
        String beds = hasText(hospital.beds()) ? hospital.beds().trim() : (existing != null ? existing.beds() : "0");

        return new Facility(
            hospital.id(),
            hospital.name().trim(),
            status,
            beds,
            hospital.distance(),
            type,
            occupancy,
            hospital.coordinates(),
            hospital.equipment(),
            hospital.facilityType().trim(),
            hospital.waitTime(),
            hospital.icu(),
            hospital.icuTotal(),
            waitType
        );
    }

    private String deriveFacilityType(int occupancy) {
        if (occupancy >= 90) {
            return "error";
        }
        if (occupancy >= 75) {
            return "warning";
        }
        return "success";
    }

    private String deriveWaitType(int occupancy) {
        return occupancy >= 85 ? "error" : "primary";
    }

    private String deriveStatusLabel(int occupancy) {
        if (occupancy >= 90) {
            return "CRITICAL (" + occupancy + "%)";
        }
        if (occupancy >= 75) {
            return "BUSY (" + occupancy + "%)";
        }
        return "STABLE (" + occupancy + "%)";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
