package com.example.ems_command_center.service;

import com.example.ems_command_center.model.DashboardStats;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final VehicleService vehicleService;
    private final FacilityService facilityService;
    private final IncidentService incidentService;

    public StatsService(VehicleService vehicleService, FacilityService facilityService, IncidentService incidentService) {
        this.vehicleService = vehicleService;
        this.facilityService = facilityService;
        this.incidentService = incidentService;
    }

    public DashboardStats getDashboardStats() {
        long activeAmbulances = vehicleService.countByStatus("busy");
        long totalAmbulances = vehicleService.countAll();
        double avgOccupancy = facilityService.getAverageOccupancy();
        long criticalIncidents = incidentService.countByStatus("Active");

        return new DashboardStats(
                (int) activeAmbulances,
                (int) totalAmbulances,
                "6m 42s",
                (int) Math.round(avgOccupancy),
                (int) criticalIncidents
        );
    }
}
