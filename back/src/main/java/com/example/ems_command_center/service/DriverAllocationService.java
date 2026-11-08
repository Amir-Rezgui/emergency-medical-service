package com.example.ems_command_center.service;

import com.example.ems_command_center.model.User;
import com.example.ems_command_center.model.Vehicle;
import com.example.ems_command_center.repository.UserRepository;
import com.example.ems_command_center.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DriverAllocationService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    public DriverAllocationService(
            UserRepository userRepository,
            VehicleRepository vehicleRepository
    ) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Allocate a driver to an ambulance
     * @param driverId ID of the driver to allocate
     * @param ambulanceId ID of the ambulance
     * @return Map containing driver, ambulance, and allocation timestamp
     */
    public Map<String, Object> allocateDriverToAmbulance(String driverId, String ambulanceId) {
        // Verify driver exists and is a driver role
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        if (!isDriver(driver)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a driver");
        }

        // Verify ambulance exists and is an ambulance type
        Vehicle ambulance = vehicleRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ambulance not found"));

        if (!"ambulance".equalsIgnoreCase(ambulance.type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle is not an ambulance");
        }

        // Update driver's ambulanceId
        driver.setAmbulanceId(ambulanceId);
        userRepository.save(driver);

        // Add driver to ambulance crew if not already present
        List<String> crew = ambulance.crew() != null ? new ArrayList<>(ambulance.crew()) : new ArrayList<>();
        if (!crew.contains(driverId)) {
            crew.add(driverId);
        }

        Vehicle updatedAmbulance = new Vehicle(
                ambulance.id(),
                ambulance.name(),
                ambulance.status(),
                ambulance.type(),
                ambulance.location(),
                driverId,
                 crew,
                LocalDateTime.now().format(DATETIME_FORMAT),
                ambulance.equipment()
        );
        vehicleRepository.save(updatedAmbulance);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("driver", driver);
        response.put("ambulance", updatedAmbulance);
        response.put("allocatedAt", LocalDateTime.now().format(DATETIME_FORMAT));
        response.put("message", "Driver successfully allocated to ambulance");

        return response;
    }

    /**
     * Deallocate a driver from an ambulance
     * @param driverId ID of the driver to deallocate
     * @param ambulanceId ID of the ambulance
     * @return Map containing driver, ambulance, and deallocation timestamp
     */
    public Map<String, Object> deallocateDriverFromAmbulance(String driverId, String ambulanceId) {
        // Verify driver exists
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        // Verify ambulance exists
        Vehicle ambulance = vehicleRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ambulance not found"));

        // Verify driver is allocated to this ambulance
        if (!ambulanceId.equals(driver.getAmbulanceId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver is not allocated to this ambulance");
        }

        // Remove ambulanceId from driver
        driver.setAmbulanceId(null);
        userRepository.save(driver);

        // Remove driver from ambulance crew
        List<String> crew = ambulance.crew() != null ? new ArrayList<>(ambulance.crew()) : new ArrayList<>();
        crew.remove(driverId);

        Vehicle updatedAmbulance = new Vehicle(
                ambulance.id(),
                ambulance.name(),
                ambulance.status(),
                ambulance.type(),
                ambulance.location(),
                ambulanceId, crew,
                LocalDateTime.now().format(DATETIME_FORMAT),
                ambulance.equipment()
        );
        vehicleRepository.save(updatedAmbulance);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("driver", driver);
        response.put("ambulance", updatedAmbulance);
        response.put("deallocatedAt", LocalDateTime.now().format(DATETIME_FORMAT));
        response.put("message", "Driver successfully deallocated from ambulance");

        return response;
    }

    /**
     * Get all drivers allocated to a specific ambulance
     * @param ambulanceId ID of the ambulance
     * @return List of drivers allocated to the ambulance
     */
    public List<User> getAmbulanceDrivers(String ambulanceId) {
        Vehicle ambulance = vehicleRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ambulance not found"));

        List<String> crewIds = ambulance.crew() != null ? ambulance.crew() : new ArrayList<>();
        List<User> drivers = new ArrayList<>();

        for (String crewId : crewIds) {
            userRepository.findById(crewId).ifPresent(user -> {
                if (isDriver(user)) {
                    drivers.add(user);
                }
            });
        }

        return drivers;
    }

    /**
     * Get the ambulance allocated to a specific driver
     * @param driverId ID of the driver
     * @return Vehicle (ambulance) allocated to the driver
     */
    public Vehicle getDriverAmbulance(String driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        String ambulanceId = driver.getAmbulanceId();
        if (ambulanceId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver is not allocated to any ambulance");
        }

        return vehicleRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ambulance not found"));
    }

    /**
     * Get allocation status for a specific driver
     * @param driverId ID of the driver
     * @return Map containing driver allocation details
     */
    public Map<String, Object> getDriverAllocationStatus(String driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("driverId", driver.getId());
        response.put("driverName", driver.getName());

        String ambulanceId = driver.getAmbulanceId();
        if (ambulanceId != null) {
            Vehicle ambulance = vehicleRepository.findById(ambulanceId)
                    .orElse(null);
            response.put("allocated", true);
            response.put("ambulanceId", ambulanceId);
            response.put("ambulance", ambulance);
        } else {
            response.put("allocated", false);
            response.put("ambulanceId", null);
            response.put("ambulance", null);
        }

        return response;
    }

    /**
     * Helper method to check if a user is a driver
     */
    private boolean isDriver(User user) {
        return "DRIVER".equalsIgnoreCase(user.getRole());
    }
}
