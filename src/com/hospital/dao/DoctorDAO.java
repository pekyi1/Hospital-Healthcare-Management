package com.hospital.dao;

import com.hospital.model.Doctor;
import com.hospital.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public void addDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (first_name, last_name, specialization, email, phone, department_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getLastName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getEmail());
            pstmt.setString(5, doctor.getPhone());
            pstmt.setInt(6, doctor.getDepartmentId());
            if (doctor.getCreatedAt() != null) {
                pstmt.setTimestamp(7, java.sql.Timestamp.valueOf(doctor.getCreatedAt()));
            } else {
                pstmt.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            }

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    doctor.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateDoctor(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctors SET first_name=?, last_name=?, specialization=?, email=?, phone=?, department_id=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getLastName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getEmail());
            pstmt.setString(5, doctor.getPhone());
            pstmt.setInt(6, doctor.getDepartmentId());
            pstmt.setInt(7, doctor.getId());

            pstmt.executeUpdate();
        }
    }

    public void deleteDoctor(int id) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // Delete related records from appointments table
                String deleteAppointmentsSql = "DELETE FROM appointments WHERE doctor_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteAppointmentsSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }

                // Delete related records from prescriptions table
                String deletePrescriptionsSql = "DELETE FROM prescriptions WHERE doctor_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(deletePrescriptionsSql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }

                // Finally delete the doctor
                String deleteDoctorSql = "DELETE FROM doctors WHERE id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteDoctorSql)) {
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

    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors";
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                doctors.add(mapResultSetToDoctor(rs));
            }
        }
        return doctors;
    }

    public Doctor getDoctorById(int id) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        }
        return null;
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        LocalDateTime createdAt = null;
        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            createdAt = ts.toLocalDateTime();
        }
        return new Doctor(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("specialization"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getInt("department_id"),
                createdAt);
    }

    public int getDoctorCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctors";
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
     * Find a doctor by first and last name (case-insensitive)
     * Returns the first match or null if not found
     */
    public Doctor getDoctorByName(String firstName, String lastName) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE LOWER(first_name) = LOWER(?) AND LOWER(last_name) = LOWER(?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName.trim());
            pstmt.setString(2, lastName.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get doctor counts grouped by department
     * Returns a map with department names as keys and doctor count as values
     */
    public java.util.Map<String, Integer> getDoctorsPerDepartment() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();

        String sql = "SELECT d.name as dept_name, COUNT(doc.id) as doctor_count " +
                "FROM departments d " +
                "LEFT JOIN doctors doc ON d.id = doc.department_id " +
                "GROUP BY d.id, d.name " +
                "ORDER BY doctor_count DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String deptName = rs.getString("dept_name");
                int count = rs.getInt("doctor_count");
                if (deptName != null && count > 0) {
                    stats.put(deptName, count);
                }
            }
        }
        return stats;
    }
}
