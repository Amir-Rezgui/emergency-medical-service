package com.example.ems_command_center.model;

import java.util.List;

public record HospitalManagerOverview(
    List<Incident> emergencies,
    List<HospitalPatient> patients,
    List<BedAvailability> beds,
    List<MedicalResource> resources,
    List<MedicalStaffMember> staff
) {
}
