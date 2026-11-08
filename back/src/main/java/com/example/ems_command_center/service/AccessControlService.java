package com.example.ems_command_center.service;

import com.example.ems_command_center.model.DispatchAssignment;
import com.example.ems_command_center.repository.DispatchAssignmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service("accessControlService")
public class AccessControlService {

    private final DispatchAssignmentRepository dispatchAssignmentRepository;

    public AccessControlService(DispatchAssignmentRepository dispatchAssignmentRepository) {
        this.dispatchAssignmentRepository = dispatchAssignmentRepository;
    }

    /**
     * Checks if the user is assigned to the specified hospital.
     */
    public boolean isAssignedHospital(Authentication authentication, String hospitalId) {
        if (hospitalId == null || hospitalId.isBlank()) {
            return false;
        }
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            String claim = jwtToken.getToken().getClaimAsString("hospital_id");
            return hospitalId.equals(claim);
        }
        return false;
    }

    /**
     * Checks if the user is assigned to the specified ambulance.
     */
    public boolean isAssignedAmbulance(Authentication authentication, String ambulanceId) {
        if (ambulanceId == null || ambulanceId.isBlank()) {
            return false;
        }
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            String claim = jwtToken.getToken().getClaimAsString("ambulance_id");
            return ambulanceId.equals(claim);
        }
        return false;
    }

    public boolean canAccessDispatchAssignment(Authentication authentication, String assignmentId) {
        if (authentication == null || assignmentId == null || assignmentId.isBlank()) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }

        DispatchAssignment assignment = dispatchAssignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            return false;
        }

        boolean isManager = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));
        if (isManager) {
            return isAssignedHospital(authentication, assignment.hospitalId());
        }

        boolean isDriver = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_DRIVER"));
        if (isDriver) {
            return isAssignedAmbulance(authentication, assignment.vehicleId());
        }

        return false;
    }
}
