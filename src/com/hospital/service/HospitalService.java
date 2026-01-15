package com.hospital.service;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DepartmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.MedicalInventory;
import com.hospital.model.Prescription;
import com.hospital.model.PatientFeedback;
import com.hospital.dao.InventoryDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.dao.FeedbackDAO;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalService {

    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;
    private final AppointmentDAO appointmentDAO;
    private final InventoryDAO inventoryDAO;
    private final PrescriptionDAO prescriptionDAO;
    private final FeedbackDAO feedbackDAO;
    private final DepartmentDAO departmentDAO;
    private final com.hospital.dao.MongoNoteDAO mongoNoteDAO;

    // In-Memory Cache
    private Map<Integer, Patient> patientCache = new HashMap<>();
    private Map<Integer, Doctor> doctorCache = new HashMap<>();
    private Map<Integer, MedicalInventory> inventoryCache = new HashMap<>();

    public HospitalService() {
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.inventoryDAO = new InventoryDAO();
        this.prescriptionDAO = new PrescriptionDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.departmentDAO = new DepartmentDAO();
        this.mongoNoteDAO = new com.hospital.dao.MongoNoteDAO();
    }

    // Performance Logger
    private void logPerformance(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        String message = String.format("[Performance] %s completed in %d ms", operation, duration);
        System.out.println(message);

        // Append to report file
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("performance_report.csv");
            String timestamp = java.time.LocalDateTime.now().toString();
            String csvLine = String.format("%s,%s,%d%n", timestamp, operation, duration);

            if (!java.nio.file.Files.exists(path)) {
                java.nio.file.Files.writeString(path, "Timestamp,Operation,Duration_ms\n");
            }
            java.nio.file.Files.writeString(path, csvLine, java.nio.file.StandardOpenOption.APPEND);
        } catch (java.io.IOException e) {
            System.err.println("Failed to write to performance report: " + e.getMessage());
        }
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

    public Patient getPatientById(int id) throws SQLException {
        // Check cache first
        if (patientCache.containsKey(id)) {
            return patientCache.get(id);
        }
        long start = System.currentTimeMillis();
        Patient patient = patientDAO.getPatientById(id);
        if (patient != null) {
            patientCache.put(id, patient);
        }
        logPerformance("getPatientById", start);
        return patient;
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

    public Patient getPatientByName(String firstName, String lastName) throws SQLException {
        long start = System.currentTimeMillis();
        Patient patient = patientDAO.getPatientByName(firstName, lastName);
        logPerformance("getPatientByName", start);
        return patient;
    }

    public java.util.Map<String, Integer> getPatientsPerDayOfWeek() throws SQLException {
        long start = System.currentTimeMillis();
        java.util.Map<String, Integer> stats = patientDAO.getPatientsPerDayOfWeek();
        logPerformance("getPatientsPerDayOfWeek", start);
        return stats;
    }

    public java.util.Map<String, Integer> getDoctorsPerDepartment() throws SQLException {
        long start = System.currentTimeMillis();
        java.util.Map<String, Integer> stats = doctorDAO.getDoctorsPerDepartment();
        logPerformance("getDoctorsPerDepartment", start);
        return stats;
    }

    /**
     * User Story 3.2: Sorting Optimization
     * Sorts patients by Last Name then First Name using Merge Sort (via
     * Collections.sort O(n log n)).
     */
    public void sortPatientsByName(List<Patient> patients) {
        long start = System.currentTimeMillis();
        patients.sort((p1, p2) -> {
            int res = p1.getLastName().compareToIgnoreCase(p2.getLastName());
            if (res == 0) {
                return p1.getFirstName().compareToIgnoreCase(p2.getFirstName());
            }
            return res;
        });
        logPerformance("sortPatientsByName (In-Memory)", start);
    }

    /**
     * User Story 3.2: Searching Optimization (Cache)
     * Searches patients from the in-memory cache to avoid DB hits.
     */
    public List<Patient> searchPatientsFromCache(String keyword) {
        long start = System.currentTimeMillis();
        String lowerKeyword = keyword.toLowerCase();
        List<Patient> results = patientCache.values().stream()
                .filter(p -> p.getFirstName().toLowerCase().contains(lowerKeyword) ||
                        p.getLastName().toLowerCase().contains(lowerKeyword))
                .toList();
        logPerformance("searchPatientsFromCache", start);
        return results;
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

    public Doctor getDoctorById(int id) throws SQLException {
        // Check cache first
        if (doctorCache.containsKey(id)) {
            return doctorCache.get(id);
        }
        long start = System.currentTimeMillis();
        Doctor doctor = doctorDAO.getDoctorById(id);
        if (doctor != null) {
            doctorCache.put(id, doctor);
        }
        logPerformance("getDoctorById", start);
        return doctor;
    }

    public void updateDoctor(Doctor doctor) throws SQLException {
        long start = System.currentTimeMillis();
        doctorDAO.updateDoctor(doctor);
        doctorCache.put(doctor.getId(), doctor);
        logPerformance("updateDoctor", start);
    }

    public void deleteDoctor(int id) throws SQLException {
        long start = System.currentTimeMillis();
        doctorDAO.deleteDoctor(id);
        doctorCache.remove(id);
        logPerformance("deleteDoctor", start);
    }

    public Doctor getDoctorByName(String firstName, String lastName) throws SQLException {
        long start = System.currentTimeMillis();
        Doctor doctor = doctorDAO.getDoctorByName(firstName, lastName);
        logPerformance("getDoctorByName", start);
        return doctor;
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

    public void updateAppointment(Appointment appointment) throws SQLException {
        long start = System.currentTimeMillis();
        appointmentDAO.updateAppointment(appointment);
        logPerformance("updateAppointment", start);
    }

    public void cancelAppointment(int id) throws SQLException {
        long start = System.currentTimeMillis();
        appointmentDAO.deleteAppointment(id);
        logPerformance("cancelAppointment", start);
    }

    // Inventory Services
    public void addInventoryItem(MedicalInventory item) throws SQLException {
        long start = System.currentTimeMillis();
        inventoryDAO.addItem(item);
        inventoryCache.put(item.getId(), item);
        logPerformance("addInventoryItem", start);
    }

    public List<MedicalInventory> getAllInventoryItems() throws SQLException {
        long start = System.currentTimeMillis();
        List<MedicalInventory> items = inventoryDAO.getAllItems();
        inventoryCache.clear();
        for (MedicalInventory item : items) {
            inventoryCache.put(item.getId(), item);
        }
        logPerformance("getAllInventoryItems", start);
        return items;
    }

    public void updateInventoryItem(MedicalInventory item) throws SQLException {
        long start = System.currentTimeMillis();
        inventoryDAO.updateItem(item);
        inventoryCache.put(item.getId(), item);
        logPerformance("updateInventoryItem", start);
    }

    public void deleteInventoryItem(int id) throws SQLException {
        long start = System.currentTimeMillis();
        inventoryDAO.deleteItem(id);
        inventoryCache.remove(id);
        logPerformance("deleteInventoryItem", start);
    }

    // Department Services
    public List<Department> getAllDepartments() throws SQLException {
        long start = System.currentTimeMillis();
        List<Department> departments = departmentDAO.getAllDepartments();
        logPerformance("getAllDepartments", start);
        return departments;
    }

    public Department getDepartmentById(int id) throws SQLException {
        long start = System.currentTimeMillis();
        Department department = departmentDAO.getDepartmentById(id);
        logPerformance("getDepartmentById", start);
        return department;
    }

    public Department getDepartmentByName(String name) throws SQLException {
        long start = System.currentTimeMillis();
        Department department = departmentDAO.getDepartmentByName(name);
        logPerformance("getDepartmentByName", start);
        return department;
    }

    public void addDepartment(Department department) throws SQLException {
        long start = System.currentTimeMillis();
        departmentDAO.addDepartment(department);
        logPerformance("addDepartment", start);
    }

    public void updateDepartment(Department department) throws SQLException {
        long start = System.currentTimeMillis();
        departmentDAO.updateDepartment(department);
        logPerformance("updateDepartment", start);
    }

    public void deleteDepartment(int id) throws SQLException {
        long start = System.currentTimeMillis();
        departmentDAO.deleteDepartment(id);
        logPerformance("deleteDepartment", start);
    }

    // Prescription Services
    public void prescribeMedication(Prescription prescription) throws SQLException {
        long start = System.currentTimeMillis();
        prescriptionDAO.addPrescription(prescription);
        logPerformance("prescribeMedication", start);
    }

    public List<Prescription> getPatientPrescriptions(int patientId) throws SQLException {
        long start = System.currentTimeMillis();
        List<Prescription> list = prescriptionDAO.getPrescriptionsByPatientId(patientId);
        logPerformance("getPatientPrescriptions", start);
        return list;
    }

    public List<Prescription> getAllPrescriptions() throws SQLException {
        long start = System.currentTimeMillis();
        List<Prescription> list = prescriptionDAO.getAllPrescriptions();
        logPerformance("getAllPrescriptions", start);
        return list;
    }

    public void updatePrescription(Prescription prescription) throws SQLException {
        long start = System.currentTimeMillis();
        prescriptionDAO.updatePrescription(prescription);
        logPerformance("updatePrescription", start);
    }

    public void deletePrescription(int id) throws SQLException {
        long start = System.currentTimeMillis();
        prescriptionDAO.deletePrescription(id);
        logPerformance("deletePrescription", start);
    }

    public List<Prescription> getAllPrescriptionsWithItems() throws SQLException {
        long start = System.currentTimeMillis();
        List<Prescription> list = prescriptionDAO.getAllPrescriptionsWithItems();
        logPerformance("getAllPrescriptionsWithItems", start);
        return list;
    }

    public void updatePrescriptionWithItems(Prescription prescription) throws SQLException {
        long start = System.currentTimeMillis();
        prescriptionDAO.updatePrescriptionWithItems(prescription);
        logPerformance("updatePrescriptionWithItems", start);
    }

    // Feedback Services
    public void submitFeedback(PatientFeedback feedback) throws SQLException {
        long start = System.currentTimeMillis();
        feedbackDAO.addFeedback(feedback);
        logPerformance("submitFeedback", start);
    }

    public List<PatientFeedback> getAllFeedback() throws SQLException {
        long start = System.currentTimeMillis();
        List<PatientFeedback> list = feedbackDAO.getAllFeedback();
        logPerformance("getAllFeedback", start);
        return list;
    }

    // Dashboard Statistics
    public int getPatientCount() throws SQLException {
        return patientDAO.getPatientCount();
    }

    public int getDoctorCount() throws SQLException {
        return doctorDAO.getDoctorCount();
    }

    public int getAppointmentCount() throws SQLException {
        return appointmentDAO.getAppointmentCount();
    }

    // Chart Data Aggregation (In-Memory for now, to support dynamic charts without
    // complex SQL)
    public Map<String, Integer> getAppointmentsPerDay() throws SQLException {
        long start = System.currentTimeMillis();
        List<Appointment> all = appointmentDAO.getAllAppointments();
        Map<String, Integer> stats = new HashMap<>();

        // Initialize days to ensure 0s are present
        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (String day : days)
            stats.put(day, 0);

        // Simple aggregation based on DayOfWeek
        // Note: Real-world app would filter by "This Week" using date comparisons.
        // Here we just map all appointments to day-of-week buckets for demonstration of
        // "Real Data from DB"
        for (Appointment a : all) {
            String day = a.getAppointmentDate().getDayOfWeek().name(); // MONDAY, TUESDAY...
            String shortDay = day.substring(0, 1).toUpperCase() + day.substring(1, 3).toLowerCase(); // Mon, Tue
            stats.put(shortDay, stats.getOrDefault(shortDay, 0) + 1);
        }

        logPerformance("getAppointmentsPerDay", start);
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

        logPerformance("getDoctorSpecializationStats", start);
        return stats;
    }

    // User Story 4.2: NosQL Patient Notes (MongoDB)
    public void addPatientNote(com.hospital.model.PatientNote note) {
        long start = System.currentTimeMillis();
        mongoNoteDAO.addNote(note);
        logPerformance("addPatientNote (MongoDB)", start);
    }

    public List<com.hospital.model.PatientNote> getPatientNotes(int patientId) {
        long start = System.currentTimeMillis();
        List<com.hospital.model.PatientNote> notes = mongoNoteDAO.getNotesByPatientId(patientId);
        logPerformance("getPatientNotes (MongoDB)", start);
        return notes;
    }
}
