package com.hospital.dao;

import com.hospital.model.Department;
import com.hospital.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments() throws SQLException {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments";
        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                departments.add(mapResultSetToDepartment(rs));
            }
        }
        return departments;
    }

    public Department getDepartmentById(int id) throws SQLException {
        String sql = "SELECT * FROM departments WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find a department by name (case-insensitive)
     * Returns the first match or null if not found
     */
    public Department getDepartmentByName(String name) throws SQLException {
        String sql = "SELECT * FROM departments WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        }
        return null;
    }

    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        return new Department(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("location"));
    }

    public void addDepartment(Department department) throws SQLException {
        String sql = "INSERT INTO departments (name, location) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, department.getName());
            pstmt.setString(2, department.getLocation());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    department.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateDepartment(Department department) throws SQLException {
        String sql = "UPDATE departments SET name=?, location=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, department.getName());
            pstmt.setString(2, department.getLocation());
            pstmt.setInt(3, department.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteDepartment(int id) throws SQLException {
        String sql = "DELETE FROM departments WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
