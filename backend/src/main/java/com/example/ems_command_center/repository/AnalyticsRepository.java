package com.example.ems_command_center.repository;

import com.example.ems_command_center.model.Analytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyticsRepository extends MongoRepository<Analytics, String> {
    Optional<Analytics> findByType(String type);
}
