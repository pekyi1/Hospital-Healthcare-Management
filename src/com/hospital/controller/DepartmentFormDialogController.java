package com.hospital.controller;

import com.hospital.model.Department;
import com.hospital.service.HospitalService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class DepartmentFormDialogController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;

    private HospitalService hospitalService;
    private Department department;
    private Stage dialogStage;
    private boolean saveSuccessful = false;

    public void initialize() {
        hospitalService = new HospitalService();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDepartment(Department department) {
        this.department = department;
        if (department != null && department.getId() > 0) {
            titleLabel.setText("Edit Department");
            nameField.setText(department.getName());
            locationField.setText(department.getLocation());
        } else {
            titleLabel.setText("Add Department");
            this.department = new Department();
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        department.setName(nameField.getText().trim());
        department.setLocation(locationField.getText().trim());

        try {
            if (department.getId() > 0) {
                hospitalService.updateDepartment(department);
            } else {
                hospitalService.addDepartment(department);
            }
            saveSuccessful = true;
            dialogStage.close();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save department: " + e.getMessage());
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

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errors.append("• Department name is required\n");
        } else if (name.length() < 2 || name.length() > 100) {
            errors.append("• Department name must be 2-100 characters\n");
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
