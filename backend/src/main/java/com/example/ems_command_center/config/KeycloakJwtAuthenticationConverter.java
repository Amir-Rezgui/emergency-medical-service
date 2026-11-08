package com.example.ems_command_center.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final String principalClaimName;
    private final String clientId;

    public KeycloakJwtAuthenticationConverter(String principalClaimName, String clientId) {
        this.principalClaimName = principalClaimName;
        this.clientId = clientId;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(scopeAuthoritiesConverter.convert(jwt));
        authorities.addAll(extractRealmRoles(jwt));
        authorities.addAll(extractClientRoles(jwt));

        String principalName = jwt.getClaimAsString(principalClaimName);
        if (principalName == null || principalName.isBlank()) {
            principalName = jwt.getSubject();
        }

        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return List.of();
        }

        Object roles = realmAccessMap.get("roles");
        if (!(roles instanceof Collection<?> roleCollection)) {
            return List.of();
        }

        return roleCollection.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(this::toRoleAuthority)
            .toList();
    }

    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (!(resourceAccess instanceof Map<?, ?> resourceAccessMap)) {
            return List.of();
        }

        Object clientAccess = resourceAccessMap.get(clientId);
        if (!(clientAccess instanceof Map<?, ?> clientAccessMap)) {
            return List.of();
        }

        Object roles = clientAccessMap.get("roles");
        if (!(roles instanceof Collection<?> roleCollection)) {
            return List.of();
        }

        return roleCollection.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(this::toRoleAuthority)
            .toList();
    }

    private GrantedAuthority toRoleAuthority(String roleName) {
        return new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase(Locale.ROOT));
    }
}
