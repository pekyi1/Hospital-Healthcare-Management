package com.hospital.controller;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.service.DepartmentService;
import com.hospital.service.DoctorService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class DoctorFormDialogController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField specializationField;
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker createdDateField;

    private DoctorService doctorService;
    private DepartmentService departmentService;

    private Doctor doctor;
    private Stage dialogStage;
    private boolean saveSuccessful = false;

    // Validation patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]{2,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\-+() ]{7,20}$");

    public void initialize() {
        doctorService = new DoctorService();
        departmentService = new DepartmentService();
        loadDepartments();
    }

    private void loadDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            departmentComboBox.setItems(FXCollections.observableArrayList(departments));

            // Set converter to display department name
            departmentComboBox.setConverter(new StringConverter<Department>() {
                @Override
                public String toString(Department dept) {
                    return dept == null ? "" : dept.getName();
                }

                @Override
                public Department fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
        } catch (SQLException e) {
            showAlert("Error", "Failed to load departments: " + e.getMessage());
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null && doctor.getId() > 0) {
            titleLabel.setText("Edit Doctor");
            firstNameField.setText(doctor.getFirstName());
            lastNameField.setText(doctor.getLastName());
            specializationField.setText(doctor.getSpecialization());
            emailField.setText(doctor.getEmail());
            phoneField.setText(doctor.getPhone());

            // Set created date if available
            if (doctor.getCreatedAt() != null) {
                createdDateField.setValue(doctor.getCreatedAt().toLocalDate());
            }

            // Select the doctor's department in the dropdown
            try {
                Department dept = departmentService.getDepartmentById(doctor.getDepartmentId());
                if (dept != null) {
                    departmentComboBox.setValue(dept);
                }
            } catch (SQLException e) {
                // Leave unselected if can't load
            }
        } else {
            titleLabel.setText("Add Doctor");
            this.doctor = new Doctor();
            // Default to today's date for new doctors
            createdDateField.setValue(LocalDate.now());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        doctor.setFirstName(firstNameField.getText().trim());
        doctor.setLastName(lastNameField.getText().trim());
        doctor.setSpecialization(specializationField.getText().trim());
        doctor.setEmail(emailField.getText().trim());
        doctor.setPhone(phoneField.getText().trim());

        Department selectedDept = departmentComboBox.getValue();
        doctor.setDepartmentId(selectedDept.getId());

        // Set created date (convert LocalDate to LocalDateTime at start of day)
        if (createdDateField.getValue() != null) {
            doctor.setCreatedAt(createdDateField.getValue().atStartOfDay());
        }

        try {
            if (doctor.getId() > 0) {
                doctorService.updateDoctor(doctor);
            } else {
                doctorService.registerDoctor(doctor);
            }
            saveSuccessful = true;
            dialogStage.close();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save doctor: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        String firstName = firstNameField.getText().trim();
        if (firstName.isEmpty()) {
            errors.append("• First name is required\n");
        } else if (!NAME_PATTERN.matcher(firstName).matches()) {
            errors.append("• First name must be 2-50 characters (letters only)\n");
        }

        String lastName = lastNameField.getText().trim();
        if (lastName.isEmpty()) {
            errors.append("• Last name is required\n");
        } else if (!NAME_PATTERN.matcher(lastName).matches()) {
            errors.append("• Last name must be 2-50 characters (letters only)\n");
        }

        String specialization = specializationField.getText().trim();
        if (specialization.isEmpty()) {
            errors.append("• Specialization is required\n");
        }

        if (departmentComboBox.getValue() == null) {
            errors.append("• Department is required\n");
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.append("• Invalid email format\n");
        }

        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            errors.append("• Invalid phone format\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString());
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
