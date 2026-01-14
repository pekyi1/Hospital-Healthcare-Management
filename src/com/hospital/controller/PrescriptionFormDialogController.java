package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.HospitalService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionFormDialogController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField patientFirstNameField;
    @FXML
    private TextField patientLastNameField;
    @FXML
    private TextField doctorFirstNameField;
    @FXML
    private TextField doctorLastNameField;
    @FXML
    private ComboBox<MedicalInventory> medicineComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private TextArea dosageField;

    private HospitalService hospitalService;
    private Prescription prescription;
    private PrescriptionItem currentItem;
    private Stage dialogStage;
    private boolean saveSuccessful = false;
    private boolean isEditing = false;

    // Store found patient/doctor
    private Patient foundPatient;
    private Doctor foundDoctor;

    public void initialize() {
        hospitalService = new HospitalService();
        loadMedicines();
    }

    private void loadMedicines() {
        try {
            List<MedicalInventory> medicines = hospitalService.getAllInventoryItems();
            medicineComboBox.setItems(FXCollections.observableArrayList(medicines));

            medicineComboBox.setConverter(new StringConverter<MedicalInventory>() {
                @Override
                public String toString(MedicalInventory item) {
                    return item == null ? "" : item.getItemName();
                }

                @Override
                public MedicalInventory fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            showAlert("Error", "Failed to load medicines: " + e.getMessage());
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
        if (prescription != null && prescription.getId() > 0) {
            titleLabel.setText("Edit Prescription");
            isEditing = true;
            loadPrescriptionData(prescription);
        } else {
            titleLabel.setText("Add Prescription");
            this.prescription = new Prescription();
            quantityField.setText("1");
        }
    }

    public void setPrescriptionForEdit(int prescriptionId, int inventoryId) {
        titleLabel.setText("Edit Prescription");
        isEditing = true;

        try {
            // Load prescription with items
            List<Prescription> prescriptions = hospitalService.getAllPrescriptionsWithItems();
            for (Prescription p : prescriptions) {
                if (p.getId() == prescriptionId) {
                    this.prescription = p;
                    loadPrescriptionData(p);

                    // Find and select the specific item
                    if (p.getItems() != null) {
                        for (PrescriptionItem item : p.getItems()) {
                            if (item.getInventoryId() == inventoryId) {
                                this.currentItem = item;
                                quantityField.setText(String.valueOf(item.getQuantity()));
                                dosageField.setText(item.getDosageInstructions());

                                // Select the medicine in dropdown
                                for (MedicalInventory med : medicineComboBox.getItems()) {
                                    if (med.getId() == inventoryId) {
                                        medicineComboBox.setValue(med);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load prescription: " + e.getMessage());
        }
    }

    private void loadPrescriptionData(Prescription prescription) {
        // Load patient name
        try {
            Patient patient = hospitalService.getPatientById(prescription.getPatientId());
            if (patient != null) {
                patientFirstNameField.setText(patient.getFirstName());
                patientLastNameField.setText(patient.getLastName());
            }
        } catch (SQLException e) {
            // Leave empty
        }

        // Load doctor name
        try {
            Doctor doctor = hospitalService.getDoctorById(prescription.getDoctorId());
            if (doctor != null) {
                doctorFirstNameField.setText(doctor.getFirstName());
                doctorLastNameField.setText(doctor.getLastName());
            }
        } catch (SQLException e) {
            // Leave empty
        }
    }

    @FXML
    private void handleSave() {
        // Validate and lookup patient
        String patientFirstName = patientFirstNameField.getText().trim();
        String patientLastName = patientLastNameField.getText().trim();

        if (patientFirstName.isEmpty() || patientLastName.isEmpty()) {
            showAlert("Validation Error", "Please enter both the patient's first and last name.");
            return;
        }

        try {
            foundPatient = hospitalService.getPatientByName(patientFirstName, patientLastName);
            if (foundPatient == null) {
                showAlert("Patient Not Found",
                        "No patient found with the name: " + patientFirstName + " " + patientLastName);
                patientFirstNameField.requestFocus();
                return;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to lookup patient: " + e.getMessage());
            return;
        }

        // Validate and lookup doctor
        String doctorFirstName = doctorFirstNameField.getText().trim();
        String doctorLastName = doctorLastNameField.getText().trim();

        if (doctorFirstName.isEmpty() || doctorLastName.isEmpty()) {
            showAlert("Validation Error", "Please enter both the doctor's first and last name.");
            return;
        }

        try {
            foundDoctor = hospitalService.getDoctorByName(doctorFirstName, doctorLastName);
            if (foundDoctor == null) {
                showAlert("Doctor Not Found",
                        "No doctor found with the name: " + doctorFirstName + " " + doctorLastName);
                doctorFirstNameField.requestFocus();
                return;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to lookup doctor: " + e.getMessage());
            return;
        }

        // Validate medicine selection
        MedicalInventory selectedMedicine = medicineComboBox.getValue();
        if (selectedMedicine == null) {
            showAlert("Validation Error", "Please select a medicine.");
            return;
        }

        // Validate quantity
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                showAlert("Validation Error", "Quantity must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid quantity number.");
            return;
        }

        String dosage = dosageField.getText().trim();
        if (dosage.isEmpty()) {
            showAlert("Validation Error", "Please enter dosage instructions.");
            return;
        }

        // Set prescription values
        prescription.setPatientId(foundPatient.getId());
        prescription.setDoctorId(foundDoctor.getId());

        // Create prescription item
        PrescriptionItem item = new PrescriptionItem();
        item.setInventoryId(selectedMedicine.getId());
        item.setQuantity(quantity);
        item.setDosageInstructions(dosage);

        List<PrescriptionItem> items = new ArrayList<>();
        items.add(item);
        prescription.setItems(items);

        try {
            if (isEditing && prescription.getId() > 0) {
                hospitalService.updatePrescriptionWithItems(prescription);
            } else {
                hospitalService.prescribeMedication(prescription);
            }
            saveSuccessful = true;
            dialogStage.close();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save prescription: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
