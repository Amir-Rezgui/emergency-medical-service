package com.example.ems_command_center.service;

import com.example.ems_command_center.model.User;
import com.example.ems_command_center.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class UserSyncService {

    private final UserRepository userRepository;

    public UserSyncService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Sync a Keycloak JWT user to MongoDB.
     * Creates a new User if none exists for the given keycloakId (sub claim).
     * Updates role-related fields if the user already exists.
     */
    public User syncUser(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        String fullName = buildFullName(firstName, lastName);
        String role = extractPrimaryRole(jwt);
        String hospitalId = jwt.getClaimAsString("hospital_id");
        String ambulanceId = jwt.getClaimAsString("ambulance_id");

        // Try to find by keycloakId first (primary lookup)
        Optional<User> existing = userRepository.findByKeycloakId(keycloakId);

        // Fallback: find by email and link keycloakId
        if (existing.isEmpty() && email != null) {
            existing = userRepository.findByEmail(email);
            if (existing.isPresent()) {
                User user = existing.get();
                user.setKeycloakId(keycloakId);
                userRepository.save(user); // CRITICAL: Save the keycloakId link immediately
            }
        }

        try {
            if (existing.isPresent()) {
                return updateExistingUser(existing.get(), fullName, email, role, hospitalId, ambulanceId);
            } else {
                return createNewUser(keycloakId, fullName, email, role, hospitalId, ambulanceId);
            }
        } catch (DuplicateKeyException e) {
            // Race condition: another request created the user simultaneously
            return userRepository.findByKeycloakId(keycloakId)
                .or(() -> email != null ? userRepository.findByEmail(email) : Optional.empty())
                .orElse(null);
        }
    }

    private User updateExistingUser(User user, String name, String email, String role, 
                                      String hospitalId, String ambulanceId) {
        boolean changed = false;

        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            changed = true;
        }
        if (email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
            changed = true;
        }
        if (role != null && !role.equals(user.getRole())) {
            user.setRole(role);
            changed = true;
        }
        if (hospitalId != null && !hospitalId.equals(user.getHospitalId())) {
            user.setHospitalId(hospitalId);
            changed = true;
        } else if (hospitalId == null && user.getHospitalId() != null && !"MANAGER".equals(role)) {
            user.setHospitalId(null);
            changed = true;
        }
        if (ambulanceId != null && !ambulanceId.equals(user.getAmbulanceId())) {
            user.setAmbulanceId(ambulanceId);
            changed = true;
        } else if (ambulanceId == null && user.getAmbulanceId() != null && !"DRIVER".equals(role)) {
            user.setAmbulanceId(null);
            changed = true;
        }

        return changed ? userRepository.save(user) : user;
    }

    private User createNewUser(String keycloakId, String name, String email, String role,
                                String hospitalId, String ambulanceId) {
        User user = new User();
        user.setKeycloakId(keycloakId);
        user.setName(name != null ? name : "Unknown");
        user.setEmail(email);
        user.setRole(role != null ? role : "USER");
        user.setHospitalId(hospitalId);
        user.setAmbulanceId(ambulanceId);
        user.setStatus("Active Now");
        user.setStatusType("success");
        return userRepository.save(user);
    }

    /**
     * Extract the primary/highest role from JWT claims.
     * Checks both realm_access.roles and resource_access.<clientId>.roles
     * Priority: ADMIN > MANAGER > DRIVER > USER
     */
    private String extractPrimaryRole(Jwt jwt) {
        // Collect all roles from both realm_access and resource_access
        List<String> allRoles = new java.util.ArrayList<>();
        
        // Check realm_access.roles
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            Object roles = realmAccessMap.get("roles");
            if (roles instanceof Collection<?> roleCollection) {
                roleCollection.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .forEach(allRoles::add);
            }
        }
        
        // Check resource_access.<clientId>.roles
        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (resourceAccess instanceof Map<?, ?> resourceAccessMap) {
            // Try common client IDs
            for (String clientId : List.of("ems-command-center", "ems-command-center-back", "account")) {
                Object clientAccess = resourceAccessMap.get(clientId);
                if (clientAccess instanceof Map<?, ?> clientAccessMap) {
                    Object roles = clientAccessMap.get("roles");
                    if (roles instanceof Collection<?> roleCollection) {
                        roleCollection.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .forEach(allRoles::add);
                    }
                }
            }
        }
        
        // Find highest priority role using case-insensitive comparison
        boolean hasAdmin = allRoles.stream()
            .anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
        boolean hasManager = allRoles.stream()
            .anyMatch(r -> r.equalsIgnoreCase("MANAGER"));
        boolean hasDriver = allRoles.stream()
            .anyMatch(r -> r.equalsIgnoreCase("DRIVER"));
        boolean hasUser = allRoles.stream()
            .anyMatch(r -> r.equalsIgnoreCase("USER"));
        
        if (hasAdmin) return "ADMIN";
        if (hasManager) return "MANAGER";
        if (hasDriver) return "DRIVER";
        if (hasUser) return "USER";
        
        return "USER";
    }

    private String buildFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return null;
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName);
        if (lastName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        return sb.toString();
    }
}
