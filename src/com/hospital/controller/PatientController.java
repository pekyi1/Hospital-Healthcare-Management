package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.HospitalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class PatientController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Patient> patientTable;
    @FXML
    private TableColumn<Patient, Integer> colId;
    @FXML
    private TableColumn<Patient, String> colFirstName;
    @FXML
    private TableColumn<Patient, String> colLastName;
    @FXML
    private TableColumn<Patient, String> colGender;
    @FXML
    private TableColumn<Patient, String> colDOB;
    @FXML
    private TableColumn<Patient, String> colEmail;
    @FXML
    private TableColumn<Patient, String> colPhone;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField genderField;
    @FXML
    private DatePicker dobField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;

    private HospitalService hospitalService;
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();

    public void initialize() {
        hospitalService = new HospitalService();
        setupTableColumns();
        loadPatients();

        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                firstNameField.setText(newSelection.getFirstName());
                lastNameField.setText(newSelection.getLastName());
                genderField.setText(newSelection.getGender());
                dobField.setValue(newSelection.getBirthDate());
                emailField.setText(newSelection.getEmail());
                phoneField.setText(newSelection.getPhone());
                addressField.setText(newSelection.getAddress());
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colDOB.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void loadPatients() {
        try {
            List<Patient> patients = hospitalService.getAllPatients();
            patientList.setAll(patients);
            patientTable.setItems(patientList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load patients: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.isEmpty()) {
            loadPatients(); // Reset to show all
            return;
        }
        try {
            List<Patient> patients = hospitalService.searchPatients(keyword);
            patientList.setAll(patients);
        } catch (SQLException e) {
            showAlert("Error", "Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        handleClear();
        loadPatients();
    }

    @FXML
    private void handleAddPatient() {
        if (!validateInput())
            return;

        Patient patient = new Patient();
        patient.setFirstName(firstNameField.getText());
        patient.setLastName(lastNameField.getText());
        patient.setGender(genderField.getText());
        patient.setBirthDate(dobField.getValue());
        patient.setEmail(emailField.getText());
        patient.setPhone(phoneField.getText());
        patient.setAddress(addressField.getText());

        try {
            hospitalService.registerPatient(patient);
            loadPatients();
            handleClear();
            showAlert("Success", "Patient added successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add patient: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdatePatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a patient to update.");
            return;
        }
        if (!validateInput())
            return;

        selected.setFirstName(firstNameField.getText());
        selected.setLastName(lastNameField.getText());
        selected.setGender(genderField.getText());
        selected.setBirthDate(dobField.getValue());
        selected.setEmail(emailField.getText());
        selected.setPhone(phoneField.getText());
        selected.setAddress(addressField.getText());

        try {
            hospitalService.updatePatient(selected);
            loadPatients();
            handleClear();
            showAlert("Success", "Patient updated successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to update patient: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        firstNameField.clear();
        lastNameField.clear();
        genderField.clear();
        dobField.setValue(null);
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        patientTable.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                emailField.getText().isEmpty() || dobField.getValue() == null) {
            showAlert("Error", "First Name, Last Name, Email, and DOB are required.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleDeletePatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a patient to delete.");
            return;
        }

        try {
            hospitalService.deletePatient(selected.getId());
            patientList.remove(selected);
            showAlert("Success", "Patient deleted successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete patient: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
