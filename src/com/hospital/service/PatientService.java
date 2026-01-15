package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.dao.MongoNoteDAO;
import com.hospital.model.Patient;
import com.hospital.model.PatientNote;
import com.hospital.util.PerformanceLogger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientService {

    private final PatientDAO patientDAO;
    private final MongoNoteDAO mongoNoteDAO;
    private Map<Integer, Patient> patientCache = new HashMap<>();

    public PatientService() {
        this.patientDAO = new PatientDAO();
        this.mongoNoteDAO = new MongoNoteDAO();
    }

    // Patient CRUD
    public void registerPatient(Patient patient) throws SQLException {
        long start = System.currentTimeMillis();
        patientDAO.addPatient(patient);
        patientCache.put(patient.getId(), patient);
        PerformanceLogger.log("registerPatient", start);
    }

    public List<Patient> getAllPatients() throws SQLException {
        long start = System.currentTimeMillis();
        List<Patient> patients = patientDAO.getAllPatients();
        patientCache.clear();
        for (Patient p : patients) {
            patientCache.put(p.getId(), p);
        }
        PerformanceLogger.log("getAllPatients (DB Scan)", start);
        return patients;
    }

    public Patient getPatientById(int id) throws SQLException {
        if (patientCache.containsKey(id)) {
            return patientCache.get(id);
        }
        long start = System.currentTimeMillis();
        Patient patient = patientDAO.getPatientById(id);
        if (patient != null) {
            patientCache.put(id, patient);
        }
        PerformanceLogger.log("getPatientById", start);
        return patient;
    }

    public void updatePatient(Patient patient) throws SQLException {
        long start = System.currentTimeMillis();
        patientDAO.updatePatient(patient);
        patientCache.put(patient.getId(), patient);
        PerformanceLogger.log("updatePatient", start);
    }

    public void deletePatient(int id) throws SQLException {
        long start = System.currentTimeMillis();
        patientDAO.deletePatient(id);
        patientCache.remove(id);
        PerformanceLogger.log("deletePatient", start);
    }

    // Searching and Sorting
    public List<Patient> searchPatients(String keyword) throws SQLException {
        long start = System.currentTimeMillis();
        List<Patient> results = patientDAO.searchPatients(keyword);
        PerformanceLogger.log("searchPatients", start);
        return results;
    }

    public List<Patient> searchPatientsFromCache(String keyword) {
        long start = System.currentTimeMillis();
        String lowerKeyword = keyword.toLowerCase();
        List<Patient> results = patientCache.values().stream()
                .filter(p -> p.getFirstName().toLowerCase().contains(lowerKeyword) ||
                        p.getLastName().toLowerCase().contains(lowerKeyword))
                .toList();
        PerformanceLogger.log("searchPatientsFromCache", start);
        return results;
    }

    public void sortPatientsByName(List<Patient> patients) {
        long start = System.currentTimeMillis();
        patients.sort((p1, p2) -> {
            int res = p1.getLastName().compareToIgnoreCase(p2.getLastName());
            if (res == 0) {
                return p1.getFirstName().compareToIgnoreCase(p2.getFirstName());
            }
            return res;
        });
        PerformanceLogger.log("sortPatientsByName (In-Memory)", start);
    }

    // Stats
    public int getPatientCount() throws SQLException {
        return patientDAO.getPatientCount();
    }

    public Map<String, Integer> getPatientsPerDayOfWeek() throws SQLException {
        long start = System.currentTimeMillis();
        Map<String, Integer> stats = patientDAO.getPatientsPerDayOfWeek();
        PerformanceLogger.log("getPatientsPerDayOfWeek", start);
        return stats;
    }

    // NoSQL Notes
    public void addPatientNote(PatientNote note) {
        long start = System.currentTimeMillis();
        mongoNoteDAO.addNote(note);
        PerformanceLogger.log("addPatientNote (MongoDB)", start);
    }

    public List<PatientNote> getPatientNotes(int patientId) {
        long start = System.currentTimeMillis();
        List<PatientNote> notes = mongoNoteDAO.getNotesByPatientId(patientId);
        PerformanceLogger.log("getPatientNotes (MongoDB)", start);
        return notes;
    }
}
