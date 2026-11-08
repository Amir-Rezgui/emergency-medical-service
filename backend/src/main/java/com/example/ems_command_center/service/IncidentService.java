package com.example.ems_command_center.service;

import com.example.ems_command_center.model.Incident;
import com.example.ems_command_center.model.IncidentEvent;
import com.example.ems_command_center.repository.IncidentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public IncidentService(IncidentRepository incidentRepository, SimpMessagingTemplate messagingTemplate) {
        this.incidentRepository = incidentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findByOrderByPriorityAsc();
    }

    public Incident getIncidentById(String id) {
        return incidentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
    }

    public Incident createIncident(Incident incident) {
        Incident savedIncident = incidentRepository.save(withTimestamp(incident, null));
        publishEvent("CREATED", savedIncident);
        publishHospitalManagerEvent("NEW_INCIDENT", savedIncident);
        return savedIncident;
    }

    public Incident updateIncident(String id, Incident incident) {
        Incident existing = incidentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));

        Incident updatedIncident = incidentRepository.save(withTimestamp(incident, existing));
        publishEvent("UPDATED", updatedIncident);
        publishHospitalManagerEvent("INCIDENT_UPDATED", updatedIncident);
        return updatedIncident;
    }

    public void deleteIncident(String id) {
        Incident existing = incidentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));

        incidentRepository.delete(existing);
        publishEvent("DELETED", existing);
        publishHospitalManagerEvent("INCIDENT_DELETED", existing);
    }

    public long countByStatus(String status) {
        return incidentRepository.findByStatus(status).size();
    }

    private Incident withTimestamp(Incident incident, Incident existing) {
        String timestamp = existing != null && existing.time() != null && !existing.time().isBlank()
            ? existing.time()
            : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return new Incident(
            existing != null ? existing.id() : incident.id(),
            incident.title(),
            incident.location(),
            incident.coordinates(),
            timestamp,
            incident.reporter(),
            incident.type(),
            incident.tags(),
            incident.status(),
            incident.priority()
        );
    }

    /**
     * Publishes to the general incidents topic.
     * Subscribers: ADMIN, USER, DRIVER (all receive general incident updates).
     */
    private void publishEvent(String action, Incident incident) {
        messagingTemplate.convertAndSend(
            "/topic/incidents",
            new IncidentEvent(action, incident.id(), incident)
        );
    }

    /**
     * Publishes to the hospital-manager topic.
     * Subscribers: MANAGER (scoped to /topic/hospital-manager/incidents).
     */
    private void publishHospitalManagerEvent(String action, Incident incident) {
        messagingTemplate.convertAndSend(
            "/topic/hospital-manager/incidents",
            new IncidentEvent(action, incident.id(), incident)
        );
    }
}
