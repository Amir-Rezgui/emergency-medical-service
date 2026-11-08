package com.example.ems_command_center.service;

import com.example.ems_command_center.model.Analytics;
import com.example.ems_command_center.model.Incident;
import com.example.ems_command_center.repository.AnalyticsRepository;
import com.example.ems_command_center.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnalyticsService {

    private static final DateTimeFormatter INCIDENT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter HOUR_LABEL_FORMAT = DateTimeFormatter.ofPattern("HH:00");

    private final AnalyticsRepository analyticsRepository;
    private final IncidentRepository incidentRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository, IncidentRepository incidentRepository) {
        this.analyticsRepository = analyticsRepository;
        this.incidentRepository = incidentRepository;
    }

    public Optional<Analytics> getByType(String type) {
        return analyticsRepository.findByType(type);
    }

    public List<Map<String, Object>> getDispatchVolume() {
        List<Map<String, Object>> liveDispatchVolume = buildLiveDispatchVolume();
        if (!liveDispatchVolume.isEmpty()) {
            return liveDispatchVolume;
        }

        return analyticsRepository.findByType("dispatch_volume")
            .map(Analytics::data)
            .map(this::normalizeDispatchVolume)
            .orElse(List.of());
    }

    public List<Map<String, Object>> getResponseTimeData() {
        return analyticsRepository.findByType("response_time")
            .map(Analytics::data)
            .orElse(List.of());
    }

    public Analytics save(Analytics analytics) {
        return analyticsRepository.save(analytics);
    }

    private List<Map<String, Object>> buildLiveDispatchVolume() {
        List<Incident> dispatchedIncidents = incidentRepository.findAll().stream()
            .filter(this::isDispatchedIncident)
            .toList();

        if (dispatchedIncidents.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> countsByHour = new HashMap<>();
        List<LocalDateTime> timestamps = new ArrayList<>();

        for (Incident incident : dispatchedIncidents) {
            LocalDateTime timestamp = parseIncidentTime(incident.time());
            if (timestamp == null) {
                continue;
            }

            timestamps.add(timestamp);
            String hourLabel = timestamp.withMinute(0).format(HOUR_LABEL_FORMAT);
            countsByHour.merge(hourLabel, 1, Integer::sum);
        }

        if (timestamps.isEmpty()) {
            return List.of();
        }

        LocalDateTime latestTimestamp = timestamps.stream().max(Comparator.naturalOrder()).orElse(LocalDateTime.now());
        List<Map<String, Object>> result = new ArrayList<>();

        for (int offset = 11; offset >= 0; offset--) {
            LocalDateTime slot = latestTimestamp.minusHours(offset).withMinute(0);
            String hourLabel = slot.format(HOUR_LABEL_FORMAT);

            Map<String, Object> point = new HashMap<>();
            point.put("time", hourLabel);
            point.put("volume", countsByHour.getOrDefault(hourLabel, 0));
            result.add(point);
        }

        return result;
    }

    private boolean isDispatchedIncident(Incident incident) {
        if (incident == null) {
            return false;
        }

        if ("dispatched".equalsIgnoreCase(incident.status())) {
            return true;
        }

        return incident.tags() != null && incident.tags().stream()
            .filter(tag -> tag != null)
            .anyMatch(tag -> tag.toLowerCase().contains("dispatched"));
    }

    private LocalDateTime parseIncidentTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, INCIDENT_TIME_FORMAT);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private List<Map<String, Object>> normalizeDispatchVolume(List<Map<String, Object>> rawData) {
        if (rawData == null || rawData.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Map<String, Object> item : rawData) {
            Map<String, Object> point = new HashMap<>();
            point.put("time", String.valueOf(item.getOrDefault("time", "00:00")));
            point.put("volume", toInteger(item.get("volume")));
            normalized.add(point);
        }
        return normalized;
    }

    private int toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }
}
