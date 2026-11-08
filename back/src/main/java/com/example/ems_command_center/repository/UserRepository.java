package com.example.ems_command_center.repository;

import com.example.ems_command_center.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByRole(String role);
    Optional<User> findByEmail(String email);
    Optional<User> findByAmbulanceId(String ambulanceId);
    Optional<User> findByHospitalId(String hospitalId);
    Optional<User> findByKeycloakId(String keycloakId);
}
