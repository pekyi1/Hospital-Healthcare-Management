package com.hospital.service;

import com.hospital.dao.FeedbackDAO;
import com.hospital.model.PatientFeedback;
import com.hospital.util.PerformanceLogger;

import java.sql.SQLException;
import java.util.List;

public class FeedbackService {

    private final FeedbackDAO feedbackDAO;

    public FeedbackService() {
        this.feedbackDAO = new FeedbackDAO();
    }

    public void submitFeedback(PatientFeedback feedback) throws SQLException {
        long start = System.currentTimeMillis();
        feedbackDAO.addFeedback(feedback);
        PerformanceLogger.log("submitFeedback", start);
    }

    public List<PatientFeedback> getAllFeedback() throws SQLException {
        long start = System.currentTimeMillis();
        List<PatientFeedback> list = feedbackDAO.getAllFeedback();
        PerformanceLogger.log("getAllFeedback", start);
        return list;
    }
}
