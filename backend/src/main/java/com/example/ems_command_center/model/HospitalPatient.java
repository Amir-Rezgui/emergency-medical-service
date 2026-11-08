package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "hospital_patients")
public record HospitalPatient(
    @Id String id,
    String patientCode,
    String patientName,
    int age,
    String emergencyId,
    String emergencyTitle,
    String triageLevel,
    String status,
    String assignedDoctor,
    String assignedNurse,
    String room,
    String dossierSummary,
    List<String> careSteps,
    boolean careValidated,
    String validatedBy,
    String updatedAt
) {
}
