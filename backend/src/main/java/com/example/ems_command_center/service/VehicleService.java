package com.example.ems_command_center.service;

import com.example.ems_command_center.model.Vehicle;
import com.example.ems_command_center.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class VehicleService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("available", "busy", "maintenance", "out-of-service", "offline");
    private static final Set<String> ALLOWED_TYPES = Set.of("ambulance", "supervisor", "fire-truck", "rescue", "other");
    private static final DateTimeFormatter UPDATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        validateVehicle(vehicle);
        return vehicleRepository.save(normalizeVehicle(vehicle, null));
    }

    public Optional<Vehicle> updateVehicle(String id, Vehicle updates) {
        return vehicleRepository.findById(id).map(existing -> {
            Vehicle updated = new Vehicle(
                existing.id(),
                hasText(updates.name()) ? updates.name() : existing.name(),
                hasText(updates.status()) ? updates.status() : existing.status(),
                hasText(updates.type()) ? updates.type() : existing.type(),
                updates.location() != null ? updates.location() : existing.location(),
                hasText(updates.driverId()) ? updates.driverId() : existing.driverId(),
                updates.crew() != null ? updates.crew() : existing.crew(),
                hasText(updates.lastUpdate()) ? updates.lastUpdate() : existing.lastUpdate(),
                updates.equipment() != null ? updates.equipment() : existing.equipment()
            );
            validateVehicle(updated);
            return vehicleRepository.save(normalizeVehicle(updated, existing));
        });
    }

    public Optional<Vehicle> updateVehicleDriver(String id, String driverId) {
        return vehicleRepository.findById(id).map(existing -> {
            Vehicle updated = new Vehicle(
                existing.id(),
                existing.name(),
                existing.status(),
                existing.type(),
                existing.location(),
                driverId,
                existing.crew(),
                LocalDateTime.now().format(UPDATE_FORMAT),
                existing.equipment()
            );
            validateVehicle(updated);
            return vehicleRepository.save(normalizeVehicle(updated, existing));
        });
    }

    public Optional<Vehicle> updateVehicleAvailability(String id) {
        return vehicleRepository.findById(id).map(existing -> {
            String newStatus = "available".equalsIgnoreCase(existing.status()) ? "busy" : "available";
            Vehicle updated = new Vehicle(
                existing.id(),
                existing.name(),
                newStatus, 
                existing.type(),
                existing.location(),
                existing.driverId(),
                existing.crew(),
                LocalDateTime.now().format(UPDATE_FORMAT),
                existing.equipment()
            );
            validateVehicle(updated);
            return vehicleRepository.save(normalizeVehicle(updated, existing));
        });
    }

    public boolean deleteVehicle(String id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public long countByStatus(String status) {
        return vehicleRepository.countByStatus(status);
    }

    public long countAll() {
        return vehicleRepository.count();
    }

    private void validateVehicle(Vehicle vehicle) {
        if (!hasText(vehicle.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle name is required");
        }
        if (!hasText(vehicle.type()) || !ALLOWED_TYPES.contains(vehicle.type().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle type must be one of: ambulance, supervisor, fire-truck, rescue, other");
        }
        if (!hasText(vehicle.status()) || !ALLOWED_STATUSES.contains(vehicle.status().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle status must be one of: available, busy, maintenance, out-of-service, offline");
        }
        if (vehicle.location() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle location is required");
        }
        if ("ambulance".equalsIgnoreCase(vehicle.type()) && "busy".equalsIgnoreCase(vehicle.status())
            && (vehicle.crew() == null || vehicle.crew().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Busy ambulances must have an assigned crew");
        }
    }

    private Vehicle normalizeVehicle(Vehicle vehicle, Vehicle existing) {
        String normalizedStatus = vehicle.status().toLowerCase();
        String normalizedType = vehicle.type().toLowerCase();
        String lastUpdate = hasText(vehicle.lastUpdate())
            ? vehicle.lastUpdate()
            : LocalDateTime.now().format(UPDATE_FORMAT);

        return new Vehicle(
            vehicle.id(),
            vehicle.name().trim(),
            normalizedStatus,
            normalizedType,
            vehicle.location(),
            vehicle.driverId(),
            vehicle.crew(),
            lastUpdate,
            vehicle.equipment() != null ? vehicle.equipment() : existing != null ? existing.equipment() : null
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
