package com.hospital.dao;

import com.hospital.model.MedicalInventory;
import com.hospital.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public void addItem(MedicalInventory item) throws SQLException {
        String sql = "INSERT INTO medical_inventory (item_name, category, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setBigDecimal(4, item.getUnitPrice());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateItem(MedicalInventory item) throws SQLException {
        String sql = "UPDATE medical_inventory SET item_name=?, category=?, quantity=?, unit_price=?, last_updated=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setBigDecimal(4, item.getUnitPrice());
            pstmt.setInt(5, item.getId());

            pstmt.executeUpdate();
        }
    }

    public void deleteItem(int id) throws SQLException {
        String sql = "DELETE FROM medical_inventory WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<MedicalInventory> getAllItems() throws SQLException {
        List<MedicalInventory> items = new ArrayList<>();
        String sql = "SELECT * FROM medical_inventory";
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public MedicalInventory getItemById(int id) throws SQLException {
        String sql = "SELECT * FROM medical_inventory WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        }
        return null;
    }

    private MedicalInventory mapResultSetToItem(ResultSet rs) throws SQLException {
        MedicalInventory item = new MedicalInventory(
                rs.getInt("id"),
                rs.getString("item_name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"));
        item.setLastUpdated(rs.getTimestamp("last_updated"));
        return item;
    }
}
