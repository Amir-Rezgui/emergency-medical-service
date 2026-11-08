package com.example.ems_command_center.config;

import com.example.ems_command_center.service.AccessControlService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final KeycloakJwtAuthenticationConverter authenticationConverter;
    private final AccessControlService accessControlService;

    public WebSocketSecurityInterceptor(
            JwtDecoder jwtDecoder,
            KeycloakJwtAuthenticationConverter authenticationConverter,
            AccessControlService accessControlService) {
        this.jwtDecoder = jwtDecoder;
        this.authenticationConverter = authenticationConverter;
        this.accessControlService = accessControlService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            if (authorization != null && !authorization.isEmpty()) {
                String bearerToken = authorization.get(0);
                if (bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    try {
                        Jwt jwt = jwtDecoder.decode(token);
                        Authentication authentication = authenticationConverter.convert(jwt);
                        accessor.setUser(authentication);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid JWT token", e);
                    }
                }
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Authentication authentication = (Authentication) accessor.getUser();
            String destination = accessor.getDestination();

            if (destination != null) {
                if (destination.startsWith("/topic/admin/")) {
                    requireAuthentication(authentication, "Authentication required for admin topics");
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    if (!isAdmin) {
                        throw new IllegalArgumentException("Only ADMIN can subscribe to admin topics");
                    }
                } else if (destination.startsWith("/topic/drivers/")) {
                    requireAuthentication(authentication, "Authentication required for drivers topic");
                    boolean isAdminOrDriver = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                    || a.getAuthority().equals("ROLE_DRIVER"));

                    if (!isAdminOrDriver) {
                        String[] parts = destination.split("/");
                        if (parts.length >= 4) {
                            String ambulanceId = parts[3];
                            if ("dispatches".equals(ambulanceId)) {
                                boolean allowedRoles = authentication.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")
                                                || a.getAuthority().equals("ROLE_DRIVER"));
                                if (!allowedRoles) {
                                    throw new IllegalArgumentException("Not authorized to subscribe to dispatches");
                                }
                            } else if (!accessControlService.isAssignedAmbulance(authentication, ambulanceId)) {
                                throw new IllegalArgumentException(
                                        "Not authorized to subscribe to this ambulance topic");
                            }
                        }
                    }
                } else if (destination.startsWith("/topic/hospital-manager/")
                        || destination.startsWith("/topic/hospitals/")) {
                    requireAuthentication(authentication, "Authentication required for hospital topics");
                    boolean isAdminOrManager = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                    || a.getAuthority().equals("ROLE_MANAGER"));

                    if (!isAdminOrManager) {
                        throw new IllegalArgumentException("Only ADMIN or MANAGER can subscribe to hospital topics");
                    }

                    if (destination.startsWith("/topic/hospitals/")) {
                        String[] parts = destination.split("/");
                        if (parts.length >= 4) {
                            String hospitalId = parts[3];
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            if (!isAdmin && !accessControlService.isAssignedHospital(authentication, hospitalId)) {
                                throw new IllegalArgumentException(
                                        "Not authorized to subscribe to this hospital topic");
                            }
                        }
                    }
                }
            }
        }

        return message;
    }

    private void requireAuthentication(Authentication authentication, String message) {
        if (authentication == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
