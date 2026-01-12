package com.hospital.model;

import java.sql.Timestamp;

public class PatientFeedback {
    private int id;
    private int patientId;
    private int rating;
    private String comments;
    private Timestamp feedbackDate;

    public PatientFeedback() {
    }

    public PatientFeedback(int id, int patientId, int rating, String comments) {
        this.id = id;
        this.patientId = patientId;
        this.rating = rating;
        this.comments = comments;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Timestamp getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(Timestamp feedbackDate) {
        this.feedbackDate = feedbackDate;
    }
}
