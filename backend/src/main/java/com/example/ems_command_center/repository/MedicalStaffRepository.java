package com.example.ems_command_center.repository;

import com.example.ems_command_center.model.MedicalStaffMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalStaffRepository extends MongoRepository<MedicalStaffMember, String> {
}
