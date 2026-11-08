package com.example.ems_command_center.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${app.security.keycloak.client-id}")
        private String clientId;

        @Value("${app.security.keycloak.principal-claim:preferred_username}")
        private String principalClaimName;

        private final UserSyncFilter userSyncFilter;

        public SecurityConfig(UserSyncFilter userSyncFilter) {
                this.userSyncFilter = userSyncFilter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(jsonAuthenticationEntryPoint())
                                                .accessDeniedHandler(jsonAccessDeniedHandler()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/api-docs/**",
                                                                "/v3/api-docs/**",
                                                                "/ws/**",
                                                                "/ws-native/**",
                                                                "/api/missions/**",
                                                                "/api/drivers/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/profile").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/users/me",
                                                                "/api/users/me/assignment")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/users/**",
                                                                "/api/hospital-manager/**")
                                                .hasAnyRole("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.GET, "/api/analytics/**")
                                                .hasAnyRole("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.GET, "/api/stats", "/api/facilities/**",
                                                                "/api/hospitals/**", "/api/incidents/**")
                                                .hasAnyRole("ADMIN", "MANAGER", "DRIVER", "USER")
                                                .requestMatchers(HttpMethod.GET, "/api/dispatch/**", "/api/vehicles/**")
                                                .hasAnyRole("ADMIN", "MANAGER", "DRIVER")
                                                .requestMatchers(HttpMethod.POST, "/api/incidents/**")
                                                .hasAnyRole("ADMIN", "MANAGER", "USER", "DRIVER")
                                                .requestMatchers(HttpMethod.POST, "/api/dispatch/**",
                                                                "/api/hospitals/**",
                                                                "/api/hospital-manager/**")
                                                .hasAnyRole("ADMIN", "MANAGER", "DRIVER")
                                                .requestMatchers(HttpMethod.PUT, "/api/dispatch/**")
                                                .hasAnyRole("ADMIN", "MANAGER", "DRIVER")
                                                .requestMatchers(HttpMethod.POST, "/api/vehicles/**")
                                                .hasAnyRole("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.PUT, "/api/vehicles/**")
                                                .hasAnyRole("ADMIN", "MANAGER", "DRIVER")
                                                .requestMatchers(HttpMethod.PUT, "/api/incidents/**",
                                                                "/api/hospitals/**",
                                                                "/api/hospital-manager/**")
                                                .hasAnyRole("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.DELETE, "/api/incidents/**")
                                                .hasAnyRole("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.DELETE, "/api/hospitals/**",
                                                                "/api/vehicles/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/api/admin/driver-allocation/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/api/**").denyAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                keycloakJwtAuthenticationConverter())))
                                .addFilterAfter(userSyncFilter,
                                                org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
                return new KeycloakJwtAuthenticationConverter(principalClaimName, clientId);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(
                                "http://localhost:5173",
                                "http://localhost:4173",
                                "http://localhost:3000",
                                "http://localhost:4200"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public OpenAPI emsOpenAPI() {
                return new OpenAPI()
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                                .schemaRequirement("bearer-jwt", new SecurityScheme()
                                                .name("bearer-jwt")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT"))
                                .info(new Info()
                                                .title("EMS Command Center API")
                                                .description("Backend REST API for the EMS Command Center emergency operations dashboard")
                                                .version("1.0.0")
                                                .license(new License().name("MIT")));
        }

        private AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
                return (request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write(
                                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"A valid Keycloak access token is required.\"}");
                };
        }

        private AccessDeniedHandler jsonAccessDeniedHandler() {
                return (request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write(
                                        "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Your Keycloak role is not allowed to access this resource.\"}");
                };
        }
}
