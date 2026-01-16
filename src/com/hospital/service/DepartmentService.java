package com.hospital.service;

import com.hospital.dao.DepartmentDAO;
import com.hospital.model.Department;
import com.hospital.util.PerformanceLogger;

import java.sql.SQLException;
import java.util.List;

public class DepartmentService {

    private final DepartmentDAO departmentDAO;

    public DepartmentService() {
        this.departmentDAO = new DepartmentDAO();
    }

    public List<Department> getAllDepartments() throws SQLException {
        long start = System.currentTimeMillis();
        List<Department> departments = departmentDAO.getAllDepartments();
        PerformanceLogger.log("getAllDepartments", start);
        return departments;
    }

    public Department getDepartmentById(int id) throws SQLException {
        long start = System.currentTimeMillis();
        Department department = departmentDAO.getDepartmentById(id);
        PerformanceLogger.log("getDepartmentById", start);
        return department;
    }

    public Department getDepartmentByName(String name) throws SQLException {
        long start = System.currentTimeMillis();
        Department department = departmentDAO.getDepartmentByName(name);
        PerformanceLogger.log("getDepartmentByName", start);
        return department;
    }

    public void addDepartment(Department department) throws SQLException {
        long start = System.currentTimeMillis();
        departmentDAO.addDepartment(department);
        PerformanceLogger.log("addDepartment", start);
    }

    public void updateDepartment(Department department) throws SQLException {
        long start = System.currentTimeMillis();
        departmentDAO.updateDepartment(department);
        PerformanceLogger.log("updateDepartment", start);
    }

    public void deleteDepartment(int id) throws SQLException {
        long start = System.currentTimeMillis();
        departmentDAO.deleteDepartment(id);
        PerformanceLogger.log("deleteDepartment", start);
    }
}
