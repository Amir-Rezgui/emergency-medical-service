package com.example.ems_command_center.seeder;

import com.example.ems_command_center.model.*;
import com.example.ems_command_center.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final IncidentRepository incidentRepository;
    private final FacilityRepository facilityRepository;
    private final VehicleRepository vehicleRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AnalyticsRepository analyticsRepository;
    private final HospitalPatientRepository hospitalPatientRepository;
    private final BedAvailabilityRepository bedAvailabilityRepository;
    private final MedicalResourceRepository medicalResourceRepository;
    private final MedicalStaffRepository medicalStaffRepository;

    public DataSeeder(
        IncidentRepository incidentRepository,
        FacilityRepository facilityRepository,
        VehicleRepository vehicleRepository,
        ReportRepository reportRepository,
        UserRepository userRepository,
        AnalyticsRepository analyticsRepository,
        HospitalPatientRepository hospitalPatientRepository,
        BedAvailabilityRepository bedAvailabilityRepository,
        MedicalResourceRepository medicalResourceRepository,
        MedicalStaffRepository medicalStaffRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.facilityRepository = facilityRepository;
        this.vehicleRepository = vehicleRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.analyticsRepository = analyticsRepository;
        this.hospitalPatientRepository = hospitalPatientRepository;
        this.bedAvailabilityRepository = bedAvailabilityRepository;
        this.medicalResourceRepository = medicalResourceRepository;
        this.medicalStaffRepository = medicalStaffRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedIncidents();
        seedFacilities();
        seedVehicles();
        seedReports();
        seedAnalytics();
        seedHospitalManagerData();
        log.info("✅ EMS Command Center — database seeding complete.");
    }

    private void seedIncidents() {
        if (incidentRepository.count() > 0) return;
        incidentRepository.saveAll(List.of(
            new Incident(null, "MVA - Multi Vehicle Collision",
                "Avenue Habib Bourguiba, Tunis",
                new Coordinates(36.8065, 10.1815),
                "4m ago", null, "urgent",
                Arrays.asList("AMB-104 En Route", "PD Notified"),
                "Active", 1),
            new Incident(null, "Cardiac Distress",
                "Rue de Marseille, Tunis",
                new Coordinates(36.8165, 10.1915),
                "12m ago", null, "normal",
                Arrays.asList("Patient Onboard"),
                "Transporting", 2)
        ));
        log.info("Seeded incidents collection.");
    }

    private void seedFacilities() {
        if (facilityRepository.count() > 0) return;
        // Overview facilities (no facilityType — general view)
        Facility f1 = new Facility(null, "Charles Nicolle Hospital", "CRITICAL (96%)",
            "04", "2.4", "error", 96,
            new Coordinates(36.7965, 10.1715), null,
            null, null, 0, 0, null);

        Facility f2 = new Facility(null, "Military Hospital of Tunis", "STABLE (64%)",
            "42", "5.1", "success", 64,
            new Coordinates(36.8265, 10.2015), null,
            null, null, 0, 0, null);

        Facility f3 = new Facility(null, "Rabta Hospital", "BUSY (81%)",
            "12", "3.8", "warning", 81,
            new Coordinates(36.7865, 10.1615), null,
            null, null, 0, 0, null);

        // Hospital-specific entries (have facilityType set)
        List<Equipment> eq1 = List.of(
            new Equipment("EQ-001", "Ventilator", "functional", "2023-10-20", 15),
            new Equipment("EQ-002", "MRI Scanner", "functional", "2023-10-15", 2),
            new Equipment("EQ-003", "Defibrillator", "needs-maintenance", "2023-09-10", 8)
        );
        Facility h1 = new Facility(null, "St. Jude Medical Center", "CRITICAL (84%)",
            "12", "1.5", "error", 84,
            new Coordinates(36.8100, 10.1800), eq1,
            "Level 1 Trauma", "14 min", 84, 100, "error");

        List<Equipment> eq2 = List.of(
            new Equipment("EQ-004", "X-Ray Machine", "functional", "2023-10-18", 4),
            new Equipment("EQ-005", "ECG Monitor", "functional", "2023-10-22", 20)
        );
        Facility h2 = new Facility(null, "Central City General", "STABLE (43%)",
            "38", "3.2", "success", 43,
            new Coordinates(36.7900, 10.1700), eq2,
            "Specialist Hub", "42 min", 52, 120, "primary");

        List<Equipment> eq3 = List.of(
            new Equipment("EQ-006", "CT Scanner", "out-of-service", "2023-10-05", 1)
        );
        Facility h3 = new Facility(null, "Northside Memorial", "BUSY (83%)",
            "6", "4.0", "warning", 83,
            new Coordinates(36.8300, 10.2100), eq3,
            "Level 2 Trauma", "28 min", 91, 110, "primary");

        facilityRepository.saveAll(List.of(f1, f2, f3, h1, h2, h3));
        log.info("Seeded facilities collection.");
    }

    private void seedVehicles() {
        if (vehicleRepository.count() > 0) return;
        vehicleRepository.saveAll(List.of(
            new Vehicle("UNIT-704", "AMB-104", "busy", "ambulance",
                new Coordinates(36.8065, 10.1815),
                "M. Ben Ali",
                Arrays.asList("M. Ben Ali", "S. Trabelsi"), "2m ago",
                List.of(
                    new Equipment("VEQ-001", "Portable Ventilator", "functional", "2023-10-24", 1),
                    new Equipment("VEQ-002", "Stretcher", "functional", "2023-10-24", 1),
                    new Equipment("VEQ-003", "Oxygen Tank", "functional", "2023-10-24", 2)
                )),
            new Vehicle("UNIT-912", "AMB-202", "available", "ambulance",
                new Coordinates(36.8165, 10.1715),
                "A. Mansour",
                Arrays.asList("A. Mansour"), "5m ago",
                List.of(
                    new Equipment("VEQ-004", "Defibrillator", "functional", "2023-10-23", 1)
                )),
            new Vehicle("UNIT-402", "SUP-01", "busy", "supervisor",
                new Coordinates(36.7965, 10.1915),
                "B. Gammarth",
                Arrays.asList("B. Gammarth"), "10m ago",
                List.of(
                    new Equipment("VEQ-005", "Radio Comms", "functional", "2023-10-20", 2)
                ))
        ));
        log.info("Seeded vehicles collection.");
    }

    private void seedReports() {
        if (reportRepository.count() > 0) return;
        reportRepository.saveAll(List.of(
            new Report(null, "Monthly_Response_Audit_Oct.pdf", "Operational Analytics", "Oct 31, 2023", "Ready"),
            new Report(null, "Critical_Incident_Review_Q3.xlsx", "Clinical Review", "Oct 28, 2023", "Ready"),
            new Report(null, "Fleet_Maintenance_Log_2023.pdf", "Resources", "Oct 25, 2023", "Archived")
        ));
        log.info("Seeded reports collection.");
    }

    private void seedAnalytics() {
        if (analyticsRepository.count() > 0) return;

        Analytics dispatch = new Analytics(null, "dispatch_volume", List.of(
            Map.of("time", "08:00", "volume", 12),
            Map.of("time", "09:00", "volume", 18),
            Map.of("time", "10:00", "volume", 22),
            Map.of("time", "11:00", "volume", 15),
            Map.of("time", "12:00", "volume", 30),
            Map.of("time", "13:00", "volume", 25),
            Map.of("time", "14:00", "volume", 20),
            Map.of("time", "15:00", "volume", 28),
            Map.of("time", "16:00", "volume", 18),
            Map.of("time", "17:00", "volume", 24),
            Map.of("time", "18:00", "volume", 20)
        ));

        Analytics response = new Analytics(null, "response_time", List.of(
            Map.of("day", "Mon", "arrival", 150, "dispatch", 120),
            Map.of("day", "Tue", "arrival", 140, "dispatch", 110),
            Map.of("day", "Wed", "arrival", 100, "dispatch", 90),
            Map.of("day", "Thu", "arrival", 120, "dispatch", 100),
            Map.of("day", "Fri", "arrival", 110, "dispatch", 95),
            Map.of("day", "Sat", "arrival", 60, "dispatch", 50),
            Map.of("day", "Sun", "arrival", 80, "dispatch", 70)
        ));

        analyticsRepository.saveAll(List.of(dispatch, response));
        log.info("Seeded analytics collection.");
    }

    private void seedHospitalManagerData() {
        if (hospitalPatientRepository.count() == 0) {
            hospitalPatientRepository.saveAll(List.of(
                new HospitalPatient(
                    null,
                    "PT-2401",
                    "Amina Ben Salem",
                    34,
                    null,
                    "MVA - Multi Vehicle Collision",
                    "Red",
                    "Under Observation",
                    "Dr. Youssef Trabelsi",
                    "Nurse Mariem Jlassi",
                    "ER-03",
                    "Polytrauma with chest pain. CT scan ordered and oxygen support initiated.",
                    List.of("Primary survey completed", "Analgesia administered", "Imaging pending"),
                    false,
                    null,
                    "2026-03-25 18:40"
                ),
                new HospitalPatient(
                    null,
                    "PT-2402",
                    "Nadia Khelifi",
                    67,
                    null,
                    "Cardiac Distress",
                    "Orange",
                    "Awaiting ICU Bed",
                    "Dr. Leila Gharbi",
                    "Nurse Hatem Saidi",
                    "CCU-Queue",
                    "Acute chest pain stabilized in ambulance. ECG abnormal, continuous monitoring required.",
                    List.of("ECG reviewed", "Cardiology consulted", "ICU transfer requested"),
                    false,
                    null,
                    "2026-03-25 19:05"
                ),
                new HospitalPatient(
                    null,
                    "PT-2403",
                    "Sami Mzoughi",
                    12,
                    null,
                    "Respiratory Distress",
                    "Yellow",
                    "Validated",
                    "Dr. Rim Ben Amor",
                    "Nurse Ines Toumi",
                    "PED-08",
                    "Nebulization completed. Pediatric team approved step-down monitoring.",
                    List.of("Nebulizer administered", "SpO2 stabilized", "Family briefed"),
                    true,
                    "Dr. Rim Ben Amor",
                    "2026-03-25 17:55"
                )
            ));
            log.info("Seeded hospital patient dossiers.");
        }

        if (bedAvailabilityRepository.count() == 0) {
            bedAvailabilityRepository.saveAll(List.of(
                new BedAvailability(null, "Emergency", 24, 19, 3, 2, "Busy"),
                new BedAvailability(null, "ICU", 16, 15, 1, 0, "Critical"),
                new BedAvailability(null, "Pediatrics", 20, 11, 7, 2, "Available"),
                new BedAvailability(null, "Surgery", 18, 12, 4, 2, "Available")
            ));
            log.info("Seeded bed availability.");
        }

        if (medicalResourceRepository.count() == 0) {
            medicalResourceRepository.saveAll(List.of(
                new MedicalResource(null, "Ventilators", "Respiratory", 4, 12, "Low", "ICU", "2026-03-25 19:10"),
                new MedicalResource(null, "Defibrillators", "Critical Care", 9, 10, "Operational", "Emergency", "2026-03-25 18:55"),
                new MedicalResource(null, "Blood Units O-", "Transfusion", 3, 14, "Low", "Blood Bank", "2026-03-25 19:00"),
                new MedicalResource(null, "Portable Monitors", "Monitoring", 7, 9, "Operational", "Triage", "2026-03-25 18:50")
            ));
            log.info("Seeded medical resources.");
        }

        if (medicalStaffRepository.count() == 0) {
            medicalStaffRepository.saveAll(List.of(
                new MedicalStaffMember(null, "Dr. Youssef Trabelsi", "Medecin urgentiste", "Trauma", "Day", "On duty"),
                new MedicalStaffMember(null, "Dr. Leila Gharbi", "Cardiologue", "Cardiac ICU", "Night", "On call"),
                new MedicalStaffMember(null, "Nurse Mariem Jlassi", "Infirmiere", "Emergency", "Day", "Available"),
                new MedicalStaffMember(null, "Nurse Hatem Saidi", "Infirmier", "Critical Care", "Night", "With patient")
            ));
            log.info("Seeded medical staff.");
        }
    }
}
