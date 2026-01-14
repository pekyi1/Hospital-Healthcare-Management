package com.hospital.dao;

import com.hospital.model.Appointment;
import com.hospital.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public void addAppointment(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, status, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDate()));
            pstmt.setString(4, appointment.getStatus());
            pstmt.setString(5, appointment.getNotes());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateAppointmentStatus(int id, String status) throws SQLException {
        String sql = "UPDATE appointments SET status=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, id);

            pstmt.executeUpdate();
        }
    }

    public void updateAppointment(Appointment appointment) throws SQLException {
        String sql = "UPDATE appointments SET patient_id=?, doctor_id=?, appointment_date=?, status=?, notes=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDate()));
            pstmt.setString(4, appointment.getStatus());
            pstmt.setString(5, appointment.getNotes());
            pstmt.setInt(6, appointment.getId());

            pstmt.executeUpdate();
        }
    }

    public void deleteAppointment(int id) throws SQLException {
        String sql = "DELETE FROM appointments WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments";
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getTimestamp("appointment_date").toLocalDateTime(),
                rs.getString("status"),
                rs.getString("notes"));
    }

    public int getAppointmentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments";
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
