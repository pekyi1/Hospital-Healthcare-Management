package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class PatientFormDialogController {

    @FXML
    private Label dialogTitle;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private ComboBox<String> genderField;
    @FXML
    private DatePicker dobField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private DatePicker createdDateField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    // Regex patterns for validation
    // Patterns moved to ValidationUtil

    private PatientService patientService;
    private Patient currentPatient; // null for new patient, populated for edit
    private boolean saveSuccessful = false;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        patientService = new PatientService();

        // Initialize gender dropdown
        genderField.setItems(FXCollections.observableArrayList("Male", "Female"));
    }

    /**
     * Set the stage for this dialog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set patient data for editing (null for new patient)
     */
    public void setPatient(Patient patient) {
        this.currentPatient = patient;

        if (patient != null) {
            // Edit mode - populate form
            dialogTitle.setText("Edit Patient");
            firstNameField.setText(patient.getFirstName());
            lastNameField.setText(patient.getLastName());
            genderField.setValue(patient.getGender());
            dobField.setValue(patient.getBirthDate());
            emailField.setText(patient.getEmail());
            phoneField.setText(patient.getPhone());
            addressField.setText(patient.getAddress());
            // Set created date if available
            if (patient.getCreatedAt() != null) {
                createdDateField.setValue(patient.getCreatedAt().toLocalDate());
            }
        } else {
            // Add mode
            dialogTitle.setText("Add New Patient");
            // Default to today's date for new patients
            createdDateField.setValue(LocalDate.now());
        }
    }

    /**
     * Handle save button click
     */
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            if (currentPatient == null) {
                // Add new patient
                Patient newPatient = new Patient();
                updatePatientFromForm(newPatient);
                patientService.registerPatient(newPatient);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Patient added successfully!");
            } else {
                // Update existing patient
                updatePatientFromForm(currentPatient);
                patientService.updatePatient(currentPatient);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Patient updated successfully!");
            }

            saveSuccessful = true;
            dialogStage.close();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to save patient: " + e.getMessage());
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Update patient object from form fields
     */
    private void updatePatientFromForm(Patient patient) {
        patient.setFirstName(firstNameField.getText().trim());
        patient.setLastName(lastNameField.getText().trim());
        patient.setGender(genderField.getValue());
        patient.setBirthDate(dobField.getValue());
        patient.setEmail(emailField.getText().trim());
        patient.setPhone(phoneField.getText().trim());
        patient.setAddress(addressField.getText().trim());
        // Set created date (convert LocalDate to LocalDateTime at start of day)
        if (createdDateField.getValue() != null) {
            patient.setCreatedAt(createdDateField.getValue().atStartOfDay());
        }
    }

    /**
     * Comprehensive input validation with detailed error messages
     */
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        // Validate First Name
        String firstName = firstNameField.getText().trim();
        if (firstName.isEmpty()) {
            errors.append("• First Name is required.\n");
        } else if (!com.hospital.util.ValidationUtil.isValidName(firstName)) {
            errors.append(
                    "• First Name must contain only letters, spaces, hyphens, or apostrophes (2-50 characters).\n");
        }

        // Validate Last Name
        String lastName = lastNameField.getText().trim();
        if (lastName.isEmpty()) {
            errors.append("• Last Name is required.\n");
        } else if (!com.hospital.util.ValidationUtil.isValidName(lastName)) {
            errors.append(
                    "• Last Name must contain only letters, spaces, hyphens, or apostrophes (2-50 characters).\n");
        }

        // Validate Gender
        if (genderField.getValue() == null || genderField.getValue().isEmpty()) {
            errors.append("• Gender is required.\n");
        }

        // Validate Date of Birth
        if (dobField.getValue() == null) {
            errors.append("• Date of Birth is required.\n");
        }

        // Validate Email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errors.append("• Email is required.\n");
        } else if (!com.hospital.util.ValidationUtil.isValidEmail(email)) {
            errors.append("• Email must be in valid format (e.g., user@example.com).\n");
        }

        // Validate Phone (optional but must be valid if provided)
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !com.hospital.util.ValidationUtil.isValidPhone(phone)) {
            errors.append("• Phone must contain exactly 10 digits (e.g., 0541234567).\n");
        }

        // Validate Address (optional but must be valid if provided)
        String address = addressField.getText().trim();
        if (!address.isEmpty() && !com.hospital.util.ValidationUtil.isValidAddress(address)) {
            errors.append(
                    "• Address must be 5-200 characters and contain only letters, numbers, and common punctuation.\n");
        }

        // Show errors if any
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Input Validation Failed",
                    "Please correct the following errors:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Check if save was successful
     */
    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }
}
