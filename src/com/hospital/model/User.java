package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Represents a user in the authentication system.
 * Users can be linked to Doctor or Patient records via referenceId.
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role; // "Admin", "Doctor", "Patient"
    private Integer referenceId; // Links to doctors.id or patients.id (null for Admin)
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public User() {
    }

    public User(int id, String username, String passwordHash, String role, Integer referenceId, boolean isActive) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.referenceId = referenceId;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isAdmin() {
        return "Admin".equals(role);
    }

    public boolean isDoctor() {
        return "Doctor".equals(role);
    }

    public boolean isPatient() {
        return "Patient".equals(role);
    }
}
