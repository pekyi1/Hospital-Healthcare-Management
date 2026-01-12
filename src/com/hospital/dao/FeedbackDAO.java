package com.hospital.dao;

import com.hospital.model.PatientFeedback;
import com.hospital.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    public void addFeedback(PatientFeedback feedback) throws SQLException {
        String sql = "INSERT INTO patient_feedback (patient_id, rating, comments) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, feedback.getPatientId());
            pstmt.setInt(2, feedback.getRating());
            pstmt.setString(3, feedback.getComments());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feedback.setId(generatedKeys.getInt(1));
                    feedback.setFeedbackDate(new Timestamp(System.currentTimeMillis())); // Approximate
                }
            }
        }
    }

    public List<PatientFeedback> getAllFeedback() throws SQLException {
        List<PatientFeedback> feedbackList = new ArrayList<>();
        String sql = "SELECT * FROM patient_feedback ORDER BY feedback_date DESC";
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PatientFeedback fb = new PatientFeedback(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("rating"),
                        rs.getString("comments"));
                fb.setFeedbackDate(rs.getTimestamp("feedback_date"));
                feedbackList.add(fb);
            }
        }
        return feedbackList;
    }
}
