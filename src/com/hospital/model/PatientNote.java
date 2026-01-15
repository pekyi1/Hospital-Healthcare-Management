package com.hospital.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a document stored in MongoDB.
 * Demonstrates NoSQL flexibility using a Map for content.
 */
public class PatientNote {
    private String id;
    private int patientId;
    private LocalDateTime createdAt;
    private String category; // e.g., "Nurse Log", "Vitals", "History"
    private Map<String, String> content; // The unstructured data

    public PatientNote() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public PatientNote(int patientId, String category, Map<String, String> content) {
        this();
        this.patientId = patientId;
        this.category = category;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }
}
