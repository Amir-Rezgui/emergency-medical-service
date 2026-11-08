package com.example.ems_command_center.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    
    @Indexed(unique = true)
    private String email;
    
    private String phone;
    private String location;
    private String specialization;
    private String role;          // USER, ADMIN, DRIVER, MANAGER
    private String status;        // e.g. "Active Now", "Off Duty"
    private String statusType;    // "success", "normal", "urgent"
    private String ambulanceId;   // DRIVER-specific
    private String hospitalId;    // MANAGER-specific
    
    // Driver-specific fields
    private String license;
    private String station;
    private Integer experience;
    private List<String> certifications;
    private Double rating;
    private Integer totalMissions;
    private Integer missionsThisMonth;
    private String avatar;
    private Coordinates currentLocation;

    @Indexed(unique = true, sparse = true)
    private String keycloakId;    // Keycloak user ID (sub claim)

    // No-arg constructor
    public User() {
    }

    // All-arg constructor
    public User(String id, String name, String email, String phone, String location, String joined,
                String specialization, String role, String status, String statusType, String iconName,
                String color, List<UserStat> stats, String ambulanceId, String hospitalId, String keycloakId,
                String license, String station, Integer experience, List<String> certifications, 
                Double rating, Integer totalMissions, Integer missionsThisMonth, String avatar) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.specialization = specialization;
        this.role = role;
        this.status = status;
        this.statusType = statusType;

        this.ambulanceId = ambulanceId;
        this.hospitalId = hospitalId;
        this.keycloakId = keycloakId;
        this.license = license;
        this.station = station;
        this.experience = experience;
        this.certifications = certifications;
        this.rating = rating;
        this.totalMissions = totalMissions;
        this.missionsThisMonth = missionsThisMonth;
        this.avatar = avatar;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public String getAmbulanceId() {
        return ambulanceId;
    }

    public void setAmbulanceId(String ambulanceId) {
        this.ambulanceId = ambulanceId;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalMissions() {
        return totalMissions;
    }

    public void setTotalMissions(Integer totalMissions) {
        this.totalMissions = totalMissions;
    }

    public Integer getMissionsThisMonth() {
        return missionsThisMonth;
    }

    public void setMissionsThisMonth(Integer missionsThisMonth) {
        this.missionsThisMonth = missionsThisMonth;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Coordinates getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Coordinates currentLocation) {
        this.currentLocation = currentLocation;
    }
}
