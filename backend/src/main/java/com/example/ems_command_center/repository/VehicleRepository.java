package com.example.ems_command_center.repository;

import com.example.ems_command_center.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    List<Vehicle> findByStatus(String status);
    List<Vehicle> findByType(String type);
    long countByStatus(String status);
}
