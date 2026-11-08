package com.example.ems_command_center.repository;

import com.example.ems_command_center.model.Incident;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends MongoRepository<Incident, String> {
    List<Incident> findByStatus(String status);
    List<Incident> findByOrderByPriorityAsc();
}
