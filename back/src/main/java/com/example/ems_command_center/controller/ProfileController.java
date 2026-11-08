package com.example.ems_command_center.controller;

import com.example.ems_command_center.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "Authenticated user profile")
public class ProfileController {

    @GetMapping
    @Operation(summary = "Retrieve the authenticated Keycloak user profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthenticatedUser> getProfile(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();

        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .sorted()
            .toList();

        return ResponseEntity.ok(new AuthenticatedUser(
            authentication.getName(),
            jwt.getSubject(),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString("given_name"),
            jwt.getClaimAsString("family_name"),
            roles,
            jwt.getClaimAsString("hospital_id"),
            jwt.getClaimAsString("ambulance_id")
        ));
    }
}
