package com.hospital.service;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalService {

    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;
    private final AppointmentDAO appointmentDAO;

    // In-Memory Cache
    private Map<Integer, Patient> patientCache = new HashMap<>();
    private Map<Integer, Doctor> doctorCache = new HashMap<>();

    public HospitalService() {
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
        this.appointmentDAO = new AppointmentDAO();
    }

    // Performance Logger
    private void logPerformance(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[Performance] " + operation + " completed in " + duration + " ms");
    }

    // Patient Services with Caching
    public void registerPatient(Patient patient) throws SQLException {
        long start = System.currentTimeMillis();
        patientDAO.addPatient(patient);
        // Add to cache
        patientCache.put(patient.getId(), patient);
        logPerformance("registerPatient", start);
    }

    public List<Patient> getAllPatients() throws SQLException {
        long start = System.currentTimeMillis();
        List<Patient> patients = patientDAO.getAllPatients();
        // Refresh cache
        patientCache.clear();
        for (Patient p : patients) {
            patientCache.put(p.getId(), p);
        }
        logPerformance("getAllPatients (DB Scan)", start);
        return patients;
    }

    public List<Patient> searchPatients(String keyword) throws SQLException {
        long start = System.currentTimeMillis();
        // Here we could search cache if fully loaded, but usually search hits DB for
        // complex queries
        // For demonstration, let's hit DB
        List<Patient> results = patientDAO.searchPatients(keyword);
        logPerformance("searchPatients", start);
        return results;
    }

    public void updatePatient(Patient patient) throws SQLException {
        long start = System.currentTimeMillis();
        patientDAO.updatePatient(patient);
        patientCache.put(patient.getId(), patient);
        logPerformance("updatePatient", start);
    }

    public void deletePatient(int id) throws SQLException {
        long start = System.currentTimeMillis();
        patientDAO.deletePatient(id);
        patientCache.remove(id);
        logPerformance("deletePatient", start);
    }

    // Doctor Services
    public void registerDoctor(Doctor doctor) throws SQLException {
        long start = System.currentTimeMillis();
        doctorDAO.addDoctor(doctor);
        doctorCache.put(doctor.getId(), doctor);
        logPerformance("registerDoctor", start);
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        long start = System.currentTimeMillis();
        List<Doctor> doctors = doctorDAO.getAllDoctors();
        // Refresh cache
        doctorCache.clear();
        for (Doctor d : doctors) {
            doctorCache.put(d.getId(), d);
        }
        logPerformance("getAllDoctors", start);
        return doctors;
    }

    // Appointment Services
    public void scheduleAppointment(Appointment appointment) throws SQLException {
        long start = System.currentTimeMillis();
        appointmentDAO.addAppointment(appointment);
        logPerformance("scheduleAppointment", start);
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        long start = System.currentTimeMillis();
        List<Appointment> appointments = appointmentDAO.getAllAppointments();
        logPerformance("getAllAppointments", start);
        return appointments;
    }
}
