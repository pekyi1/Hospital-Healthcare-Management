package com.hospital.dao;

import com.hospital.model.Patient;
import com.hospital.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public void addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (first_name, last_name, gender, birth_date, email, phone, address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setString(3, patient.getGender());
            pstmt.setDate(4, Date.valueOf(patient.getBirthDate()));
            pstmt.setString(5, patient.getEmail());
            pstmt.setString(6, patient.getPhone());
            pstmt.setString(7, patient.getAddress());
            if (patient.getCreatedAt() != null) {
                pstmt.setTimestamp(8, java.sql.Timestamp.valueOf(patient.getCreatedAt()));
            } else {
                pstmt.setTimestamp(8, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            }

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    patient.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET first_name=?, last_name=?, gender=?, birth_date=?, email=?, phone=?, address=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setString(3, patient.getGender());
            pstmt.setDate(4, Date.valueOf(patient.getBirthDate()));
            pstmt.setString(5, patient.getEmail());
            pstmt.setString(6, patient.getPhone());
            pstmt.setString(7, patient.getAddress());
            pstmt.setInt(8, patient.getId());

            pstmt.executeUpdate();
        }
    }

    public void deletePatient(int id) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // Delete related records from patient_feedback table
                String deleteFeedbackSql = "DELETE FROM patient_feedback WHERE patient_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteFeedbackSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }

                // Delete related records from appointments table
                String deleteAppointmentsSql = "DELETE FROM appointments WHERE patient_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentsSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }

                // Delete related records from prescriptions table
                String deletePrescriptionsSql = "DELETE FROM prescriptions WHERE patient_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(deletePrescriptionsSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }

                // Finally delete the patient
                String deletePatientSql = "DELETE FROM patients WHERE id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(deletePatientSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }

                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Patient getPatientById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        }
        return null;
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        }
        return patients;
    }

    public List<Patient> searchPatients(String keyword) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        }
        return patients;
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        LocalDateTime createdAt = null;
        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            createdAt = ts.toLocalDateTime();
        }
        return new Patient(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("gender"),
                rs.getDate("birth_date").toLocalDate(),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                createdAt);
    }

    public int getPatientCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Find a patient by first and last name (case-insensitive)
     * Returns the first match or null if not found
     */
    public Patient getPatientByName(String firstName, String lastName) throws SQLException {
        String sql = "SELECT * FROM patients WHERE LOWER(first_name) = LOWER(?) AND LOWER(last_name) = LOWER(?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName.trim());
            pstmt.setString(2, lastName.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get patient counts grouped by day of week based on when they were
     * created/registered
     * Returns a map with day names (Mon, Tue, etc.) as keys
     */
    public java.util.Map<String, Integer> getPatientsPerDayOfWeek() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        // Initialize with zero for all days
        stats.put("Mon", 0);
        stats.put("Tue", 0);
        stats.put("Wed", 0);
        stats.put("Thu", 0);
        stats.put("Fri", 0);
        stats.put("Sat", 0);
        stats.put("Sun", 0);

        // PostgreSQL: EXTRACT(DOW FROM date) returns 0=Sunday, 1=Monday, etc.
        // Using created_at timestamp to group patients by day of week they were
        // registered
        String sql = "SELECT EXTRACT(DOW FROM created_at) as day_num, COUNT(*) as count " +
                "FROM patients " +
                "WHERE created_at IS NOT NULL " +
                "GROUP BY EXTRACT(DOW FROM created_at)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int dayNum = rs.getInt("day_num");
                int count = rs.getInt("count");
                String dayName = getDayName(dayNum);
                stats.put(dayName, count);
            }
        }
        return stats;
    }

    private String getDayName(int dayOfWeek) {
        // PostgreSQL DOW: 0=Sunday, 1=Monday, 2=Tuesday, etc.
        switch (dayOfWeek) {
            case 0:
                return "Sun";
            case 1:
                return "Mon";
            case 2:
                return "Tue";
            case 3:
                return "Wed";
            case 4:
                return "Thu";
            case 5:
                return "Fri";
            case 6:
                return "Sat";
            default:
                return "Unknown";
        }
    }
}
