package com.example.ems_command_center.service;

import com.example.ems_command_center.model.DispatchAssignment;
import com.example.ems_command_center.model.DriverAssignment;
import com.example.ems_command_center.model.User;
import com.example.ems_command_center.model.Vehicle;
import com.example.ems_command_center.repository.DispatchAssignmentRepository;
import com.example.ems_command_center.repository.UserRepository;
import com.example.ems_command_center.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchAssignmentRepository dispatchAssignmentRepository;

    public UserService(
        UserRepository userRepository,
        VehicleRepository vehicleRepository,
        DispatchAssignmentRepository dispatchAssignmentRepository
    ) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.dispatchAssignmentRepository = dispatchAssignmentRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));
    }

    public User getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User updateDriverStatus(String driverId, String status) {
        User user = getUserById(driverId);
        user.setStatus(status);
        return userRepository.save(user);
    }

    public void updateDriverLocation(String driverId, com.example.ems_command_center.model.Coordinates location) {
        User user = getUserById(driverId);
        user.setCurrentLocation(location);
        userRepository.save(user);
    }
}
