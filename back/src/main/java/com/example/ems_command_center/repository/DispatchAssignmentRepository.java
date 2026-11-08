package com.example.ems_command_center.repository;

import com.example.ems_command_center.model.AssignmentState;
import com.example.ems_command_center.model.DispatchAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DispatchAssignmentRepository extends MongoRepository<DispatchAssignment, String> {
    Optional<DispatchAssignment> findFirstByVehicleIdOrderByCreatedAtDesc(String vehicleId);
    Optional<DispatchAssignment> findFirstByDriverIdOrderByCreatedAtDesc(String driverId);
    List<DispatchAssignment> findByHospitalIdOrderByCreatedAtDesc(String hospitalId);
    List<DispatchAssignment> findByVehicleIdOrderByCreatedAtDesc(String vehicleId);
}
