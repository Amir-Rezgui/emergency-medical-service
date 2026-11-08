package com.example.ems_command_center.repository;

import com.example.ems_command_center.model.Facility;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends MongoRepository<Facility, String> {
    List<Facility> findByFacilityTypeNotNull();
}
