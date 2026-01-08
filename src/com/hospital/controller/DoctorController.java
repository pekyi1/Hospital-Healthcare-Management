package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.service.HospitalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class DoctorController {

    @FXML
    private TableView<Doctor> doctorTable;
    @FXML
    private TableColumn<Doctor, Integer> colId;
    @FXML
    private TableColumn<Doctor, String> colFirstName;
    @FXML
    private TableColumn<Doctor, String> colLastName;
    @FXML
    private TableColumn<Doctor, String> colSpecialization;
    @FXML
    private TableColumn<Doctor, String> colEmail;
    @FXML
    private TableColumn<Doctor, String> colPhone;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField specializationField;
    @FXML
    private TextField deptIdField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    private HospitalService hospitalService;
    private ObservableList<Doctor> doctorList = FXCollections.observableArrayList();

    public void initialize() {
        hospitalService = new HospitalService();
        setupTableColumns();
        loadDoctors();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void loadDoctors() {
        try {
            List<Doctor> doctors = hospitalService.getAllDoctors();
            doctorList.setAll(doctors);
            doctorTable.setItems(doctorList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load doctors: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddDoctor() {
        if (!validateInput())
            return;

        Doctor doctor = new Doctor();
        doctor.setFirstName(firstNameField.getText());
        doctor.setLastName(lastNameField.getText());
        doctor.setSpecialization(specializationField.getText());
        try {
            doctor.setDepartmentId(Integer.parseInt(deptIdField.getText()));
        } catch (NumberFormatException e) {
            showAlert("Error", "Department ID must be a number.");
            return;
        }
        doctor.setEmail(emailField.getText());
        doctor.setPhone(phoneField.getText());

        try {
            hospitalService.registerDoctor(doctor);
            loadDoctors();
            clearFields();
            showAlert("Success", "Doctor added successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add doctor: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadDoctors();
    }

    private boolean validateInput() {
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                specializationField.getText().isEmpty() || deptIdField.getText().isEmpty()) {
            showAlert("Error", "Please fill in all required fields.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        specializationField.clear();
        deptIdField.clear();
        emailField.clear();
        phoneField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
