package com.hospital.dao;

import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import com.hospital.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public void addPrescription(Prescription prescription) throws SQLException {
        String sql = "INSERT INTO prescriptions (patient_id, doctor_id, appointment_id, notes) VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO prescription_items (prescription_id, inventory_id, quantity, dosage_instructions) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // Insert Header
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, prescription.getPatientId());
                pstmt.setInt(2, prescription.getDoctorId());
                if (prescription.getAppointmentId() != null) {
                    pstmt.setInt(3, prescription.getAppointmentId());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }
                pstmt.setString(4, prescription.getNotes());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        prescription.setId(generatedKeys.getInt(1));
                        prescription.setPrescriptionDate(new Timestamp(System.currentTimeMillis()));
                    }
                }
            }

            // Insert Items
            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                for (PrescriptionItem item : prescription.getItems()) {
                    itemStmt.setInt(1, prescription.getId());
                    itemStmt.setInt(2, item.getInventoryId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setString(4, item.getDosageInstructions());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            conn.commit(); // Commit Transaction
        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Prescription> getPrescriptionsByPatientId(int patientId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? ORDER BY prescription_date DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Prescription p = new Prescription(
                            rs.getInt("id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            (Integer) rs.getObject("appointment_id"),
                            rs.getString("notes"));
                    p.setPrescriptionDate(rs.getTimestamp("prescription_date"));
                    // Load items (could be done lazily, but eager is simpler here)
                    p.setItems(getPrescriptionItems(p.getId(), conn));
                    prescriptions.add(p);
                }
            }
        }
        return prescriptions;
    }

    private List<PrescriptionItem> getPrescriptionItems(int prescriptionId, Connection conn) throws SQLException {
        List<PrescriptionItem> items = new ArrayList<>();
        String sql = "SELECT pi.*, mi.item_name FROM prescription_items pi " +
                "JOIN medical_inventory mi ON pi.inventory_id = mi.id " +
                "WHERE pi.prescription_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, prescriptionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PrescriptionItem item = new PrescriptionItem(
                            rs.getInt("id"),
                            rs.getInt("prescription_id"),
                            rs.getInt("inventory_id"),
                            rs.getInt("quantity"),
                            rs.getString("dosage_instructions"));
                    item.setMedicineName(rs.getString("item_name"));
                    items.add(item);
                }
            }
        }
        return items;
    }
}
