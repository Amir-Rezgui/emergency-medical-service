package com.example.ems_command_center.service;

import com.example.ems_command_center.model.AmbulanceRouteResponse;
import com.example.ems_command_center.model.AssignmentState;
import com.example.ems_command_center.model.Coordinates;
import com.example.ems_command_center.model.DispatchAssignment;
import com.example.ems_command_center.model.DispatchAssignmentResponse;
import com.example.ems_command_center.model.DispatchRequest;
import com.example.ems_command_center.model.Facility;
import com.example.ems_command_center.model.Incident;
import com.example.ems_command_center.model.User;
import com.example.ems_command_center.model.Vehicle;
import com.example.ems_command_center.repository.DispatchAssignmentRepository;
import com.example.ems_command_center.repository.FacilityRepository;
import com.example.ems_command_center.repository.IncidentRepository;
import com.example.ems_command_center.repository.UserRepository;
import com.example.ems_command_center.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DispatchService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VehicleRepository vehicleRepository;
    private final IncidentRepository incidentRepository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final DispatchAssignmentRepository dispatchAssignmentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DispatchService(
        VehicleRepository vehicleRepository,
        IncidentRepository incidentRepository,
        FacilityRepository facilityRepository,
        UserRepository userRepository,
        DispatchAssignmentRepository dispatchAssignmentRepository,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.vehicleRepository = vehicleRepository;
        this.incidentRepository = incidentRepository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.dispatchAssignmentRepository = dispatchAssignmentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Vehicle> getAvailableAmbulances() {
        return vehicleRepository.findByType("ambulance").stream()
            .filter(vehicle -> "available".equalsIgnoreCase(vehicle.status()))
            .toList();
    }

    public AmbulanceRouteResponse getRoute(String vehicleId, String incidentId) {
        Vehicle vehicle = getVehicle(vehicleId);
        Incident incident = getIncident(incidentId);
        validateDispatchableVehicle(vehicle);
        return buildRoute(vehicle, incident);
    }

    public DispatchAssignmentResponse getAssignmentById(String assignmentId) {
        return toResponse(getAssignmentEntity(assignmentId));
    }

    public List<DispatchAssignmentResponse> getAssignmentsByHospital(String hospitalId) {
        if (isBlank(hospitalId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hospitalId is required");
        }

        getHospital(hospitalId);
        return dispatchAssignmentRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId).stream()
            .map(this::toResponse)
            .toList();
    }

    public List<DispatchAssignmentResponse> getAssignmentsByVehicle(String vehicleId) {
        if (isBlank(vehicleId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "vehicleId is required");
        }

        getVehicle(vehicleId);
        return dispatchAssignmentRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId).stream()
            .map(this::toResponse)
            .toList();
    }

    public DispatchAssignmentResponse dispatchAmbulance(DispatchRequest request) {
        validateDispatchRequest(request);

        Vehicle vehicle = getVehicle(request.vehicleId());
        Incident incident = getIncident(request.incidentId());
        Facility hospital = getHospital(request.hospitalId());
        User driver = userRepository.findByAmbulanceId(vehicle.id()).orElse(null);

        validateDispatchableVehicle(vehicle);

        AmbulanceRouteResponse route = buildRoute(vehicle, incident);
        LocalDateTime now = LocalDateTime.now();
        String dispatchedAt = now.format(TIMESTAMP_FORMATTER);
        String dispatcher = request.dispatcher() == null || request.dispatcher().isBlank()
            ? "Dispatch Center"
            : request.dispatcher().trim();

        Vehicle dispatchedVehicle = new Vehicle(
            vehicle.id(),
            vehicle.name(),
            "busy",
            vehicle.type(),
            vehicle.location(),
            dispatcher, vehicle.crew(),
            dispatchedAt,
            vehicle.equipment()
        );

        List<String> updatedTags = new ArrayList<>(incident.tags() == null ? List.of() : incident.tags());
        updatedTags.removeIf(tag -> tag != null && (
            tag.startsWith(vehicle.name())
                || tag.startsWith("Dispatcher: ")
                || tag.startsWith("Notes: ")
                || tag.startsWith("Hospital: ")
        ));
        updatedTags.add(vehicle.name() + " Dispatched");
        updatedTags.add("Dispatcher: " + dispatcher);
        updatedTags.add("Hospital: " + hospital.name());
        if (request.notes() != null && !request.notes().isBlank()) {
            updatedTags.add("Notes: " + request.notes().trim());
        }

        Incident updatedIncident = new Incident(
            incident.id(),
            incident.title(),
            incident.location(),
            incident.coordinates(),
            incident.time(),
            incident.reporter(),
            incident.type(),
            updatedTags,
            "Dispatched",
            incident.priority()
        );

        vehicleRepository.save(dispatchedVehicle);
        incidentRepository.save(updatedIncident);

        DispatchAssignment savedAssignment = dispatchAssignmentRepository.save(new DispatchAssignment(
            null,
            updatedIncident.id(),
            updatedIncident.title(),
            dispatchedVehicle.id(),
            dispatchedVehicle.name(),
            driver != null ? driver.getId() : null,
            driver != null ? driver.getName() : null,
            hospital.id(),
            hospital.name(),
            dispatcher,
            request.notes(),
            dispatchedVehicle.status(),
            AssignmentState.ASSIGNED,
            dispatchedAt,
            now,
            updatedIncident.tags(),
            route
        ));

        DispatchAssignmentResponse response = toResponse(savedAssignment);
        publishDispatchNotifications(response);
        return response;
    }

    private void validateDispatchRequest(DispatchRequest request) {
        if (request == null || isBlank(request.incidentId()) || isBlank(request.vehicleId()) || isBlank(request.hospitalId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "incidentId, vehicleId, and hospitalId are required");
        }
    }

    private DispatchAssignment getAssignmentEntity(String assignmentId) {
        return dispatchAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispatch assignment not found"));
    }

    private Vehicle getVehicle(String vehicleId) {
        return vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    private Incident getIncident(String incidentId) {
        return incidentRepository.findById(incidentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
    }

    private Facility getHospital(String hospitalId) {
        Facility hospital = facilityRepository.findById(hospitalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));

        if (isBlank(hospital.facilityType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected facility is not a hospital");
        }

        return hospital;
    }

    private void validateDispatchableVehicle(Vehicle vehicle) {
        if (!"ambulance".equalsIgnoreCase(vehicle.type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ambulance vehicles can be dispatched");
        }
    }

    private AmbulanceRouteResponse buildRoute(Vehicle vehicle, Incident incident) {
        Coordinates origin = vehicle.location();
        Coordinates destination = incident.coordinates();
        if (origin == null || destination == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle and incident must both have coordinates");
        }

        double distanceKm = haversineKm(origin, destination);
        int estimatedMinutes = Math.max(3, (int) Math.ceil(distanceKm / averageSpeedFor(incident.type()) * 60));
        List<Coordinates> path = List.of(
            origin,
            interpolate(origin, destination, 0.33),
            interpolate(origin, destination, 0.66),
            destination
        );

        return new AmbulanceRouteResponse(
            vehicle.id(),
            vehicle.name(),
            incident.id(),
            incident.title(),
            origin,
            destination,
            path,
            round(distanceKm),
            estimatedMinutes,
            trafficFor(distanceKm, incident.type()),
            List.of(
                "Depart from current ambulance position",
                "Proceed toward " + incident.location(),
                "Maintain EMS priority response protocol",
                "Arrive on scene and update incident status"
            )
        );
    }

    private DispatchAssignmentResponse toResponse(DispatchAssignment assignment) {
        return new DispatchAssignmentResponse(
            assignment.id(),
            assignment.incidentId(),
            assignment.incidentTitle(),
            assignment.vehicleId(),
            assignment.vehicleName(),
            assignment.driverId(),
            assignment.driverName(),
            assignment.hospitalId(),
            assignment.hospitalName(),
            assignment.dispatcher(),
            assignment.notes(),
            assignment.vehicleStatus(),
            assignment.state(),
            assignment.dispatchedAt(),
            assignment.incidentTags(),
            assignment.route()
        );
    }

    private double averageSpeedFor(String incidentType) {
        return "urgent".equalsIgnoreCase(incidentType) ? 52.0 : 38.0;
    }

    private String trafficFor(double distanceKm, String incidentType) {
        if ("urgent".equalsIgnoreCase(incidentType)) {
            return distanceKm > 6 ? "moderate" : "clear";
        }
        return distanceKm > 5 ? "moderate" : "light";
    }

    private Coordinates interpolate(Coordinates origin, Coordinates destination, double ratio) {
        double lat = origin.lat() + (destination.lat() - origin.lat()) * ratio;
        double lng = origin.lng() + (destination.lng() - origin.lng()) * ratio;
        return new Coordinates(round(lat), round(lng));
    }

    private double haversineKm(Coordinates origin, Coordinates destination) {
        double earthRadiusKm = 6371.0;
        double latDistance = Math.toRadians(destination.lat() - origin.lat());
        double lonDistance = Math.toRadians(destination.lng() - origin.lng());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(origin.lat())) * Math.cos(Math.toRadians(destination.lat()))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void publishDispatchNotifications(DispatchAssignmentResponse response) {
        messagingTemplate.convertAndSend("/topic/admin/dispatches", response);
        messagingTemplate.convertAndSend("/topic/drivers/" + response.vehicleId() + "/dispatches", response);
        messagingTemplate.convertAndSend("/topic/hospitals/" + response.hospitalId() + "/dispatches", response);
    }
    public void updateAssignmentState(@NonNull String assignmentId, AssignmentState state) {
        DispatchAssignment assignment = dispatchAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        DispatchAssignment updatedAssignment = new DispatchAssignment(
            assignment.id(),
            assignment.incidentId(),
            assignment.incidentTitle(),
            assignment.vehicleId(),
            assignment.vehicleName(),
            assignment.driverId(),
            assignment.driverName(),
            assignment.hospitalId(),
            assignment.hospitalName(),
            assignment.dispatcher(),
            assignment.notes(),
            assignment.vehicleStatus(),
            state,
            assignment.dispatchedAt(),
            assignment.createdAt(),
            assignment.incidentTags(),
            assignment.route()
        );

        dispatchAssignmentRepository.save(updatedAssignment);

        // Update vehicle status if assignment is finished
        if (state == AssignmentState.COMPLETED || state == AssignmentState.CANCELLED) {
            vehicleRepository.findById(assignment.vehicleId()).ifPresent(vehicle -> {
                Vehicle availableVehicle = new Vehicle(
                    vehicle.id(),
                    vehicle.name(),
                    "available",
                    vehicle.type(),
                    vehicle.location(),
                    null,
                    vehicle.crew(),
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                    vehicle.equipment()
                );
                vehicleRepository.save(availableVehicle);
            });
        }

        DispatchAssignmentResponse response = toResponse(updatedAssignment);
        publishDispatchNotifications(response);
    }
}
