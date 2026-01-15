package com.hospital.service;

import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;
import com.hospital.util.PerformanceLogger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorService {

    private final DoctorDAO doctorDAO;
    private Map<Integer, Doctor> doctorCache = new HashMap<>();

    public DoctorService() {
        this.doctorDAO = new DoctorDAO();
    }

    public void registerDoctor(Doctor doctor) throws SQLException {
        long start = System.currentTimeMillis();
        doctorDAO.addDoctor(doctor);
        doctorCache.put(doctor.getId(), doctor);
        PerformanceLogger.log("registerDoctor", start);
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        long start = System.currentTimeMillis();
        List<Doctor> doctors = doctorDAO.getAllDoctors();
        doctorCache.clear();
        for (Doctor d : doctors) {
            doctorCache.put(d.getId(), d);
        }
        PerformanceLogger.log("getAllDoctors", start);
        return doctors;
    }

    public Doctor getDoctorById(int id) throws SQLException {
        if (doctorCache.containsKey(id)) {
            return doctorCache.get(id);
        }
        long start = System.currentTimeMillis();
        Doctor doctor = doctorDAO.getDoctorById(id);
        if (doctor != null) {
            doctorCache.put(id, doctor);
        }
        PerformanceLogger.log("getDoctorById", start);
        return doctor;
    }

    public void updateDoctor(Doctor doctor) throws SQLException {
        long start = System.currentTimeMillis();
        doctorDAO.updateDoctor(doctor);
        doctorCache.put(doctor.getId(), doctor);
        PerformanceLogger.log("updateDoctor", start);
    }

    public void deleteDoctor(int id) throws SQLException {
        long start = System.currentTimeMillis();
        doctorDAO.deleteDoctor(id);
        doctorCache.remove(id);
        PerformanceLogger.log("deleteDoctor", start);
    }

    public Doctor getDoctorByName(String firstName, String lastName) throws SQLException {
        long start = System.currentTimeMillis();
        Doctor doctor = doctorDAO.getDoctorByName(firstName, lastName);
        PerformanceLogger.log("getDoctorByName", start);
        return doctor;
    }

    // Stats
    public int getDoctorCount() throws SQLException {
        return doctorDAO.getDoctorCount();
    }

    public Map<String, Integer> getDoctorsPerDepartment() throws SQLException {
        long start = System.currentTimeMillis();
        Map<String, Integer> stats = doctorDAO.getDoctorsPerDepartment();
        PerformanceLogger.log("getDoctorsPerDepartment", start);
        return stats;
    }

    public Map<String, Integer> getDoctorSpecializationStats() throws SQLException {
        long start = System.currentTimeMillis();
        List<Doctor> all = doctorDAO.getAllDoctors();
        Map<String, Integer> stats = new HashMap<>();

        for (Doctor d : all) {
            String spec = d.getSpecialization();
            if (spec == null || spec.isEmpty())
                spec = "Unknown";
            stats.put(spec, stats.getOrDefault(spec, 0) + 1);
        }

        PerformanceLogger.log("getDoctorSpecializationStats", start);
        return stats;
    }
}
