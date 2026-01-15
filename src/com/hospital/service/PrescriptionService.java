package com.hospital.service;

import com.hospital.dao.PrescriptionDAO;
import com.hospital.model.Prescription;
import com.hospital.util.PerformanceLogger;

import java.sql.SQLException;
import java.util.List;

public class PrescriptionService {

    private final PrescriptionDAO prescriptionDAO;

    public PrescriptionService() {
        this.prescriptionDAO = new PrescriptionDAO();
    }

    public void prescribeMedication(Prescription prescription) throws SQLException {
        long start = System.currentTimeMillis();
        prescriptionDAO.addPrescription(prescription);
        PerformanceLogger.log("prescribeMedication", start);
    }

    public List<Prescription> getPatientPrescriptions(int patientId) throws SQLException {
        long start = System.currentTimeMillis();
        List<Prescription> list = prescriptionDAO.getPrescriptionsByPatientId(patientId);
        PerformanceLogger.log("getPatientPrescriptions", start);
        return list;
    }

    public List<Prescription> getAllPrescriptions() throws SQLException {
        long start = System.currentTimeMillis();
        List<Prescription> list = prescriptionDAO.getAllPrescriptions();
        PerformanceLogger.log("getAllPrescriptions", start);
        return list;
    }

    public void updatePrescription(Prescription prescription) throws SQLException {
        long start = System.currentTimeMillis();
        prescriptionDAO.updatePrescription(prescription);
        PerformanceLogger.log("updatePrescription", start);
    }

    public void deletePrescription(int id) throws SQLException {
        long start = System.currentTimeMillis();
        prescriptionDAO.deletePrescription(id);
        PerformanceLogger.log("deletePrescription", start);
    }

    public List<Prescription> getAllPrescriptionsWithItems() throws SQLException {
        long start = System.currentTimeMillis();
        List<Prescription> list = prescriptionDAO.getAllPrescriptionsWithItems();
        PerformanceLogger.log("getAllPrescriptionsWithItems", start);
        return list;
    }

    public void updatePrescriptionWithItems(Prescription prescription) throws SQLException {
        long start = System.currentTimeMillis();
        prescriptionDAO.updatePrescriptionWithItems(prescription);
        PerformanceLogger.log("updatePrescriptionWithItems", start);
    }
}
