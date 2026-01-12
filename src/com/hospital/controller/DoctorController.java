package com.hospital.controller;

import com.hospital.model.Doctor;
import com.hospital.service.HospitalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
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
    private TableColumn<Doctor, Void> colSelect; // For styling
    @FXML
    private TextField searchField; // Added for new UI

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

        doctorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Add Checkbox implementation for colSelect if strictly needed,
        // ignoring for now to keep focus on layout stability.
    }

    private void populateForm(Doctor d) {
        firstNameField.setText(d.getFirstName());
        lastNameField.setText(d.getLastName());
        specializationField.setText(d.getSpecialization());
        deptIdField.setText(String.valueOf(d.getDepartmentId()));
        emailField.setText(d.getEmail());
        phoneField.setText(d.getPhone());
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
    private void handleSearch() {
        // Stub for search functionality
        String keyword = searchField.getText();
        if (keyword == null || keyword.isEmpty()) {
            loadDoctors();
            return;
        }
        // Ideally: hospitalService.searchDoctors(keyword);
        // For now, reload all or filter logically if service supports it.
        // Alerting to avoid crash if service missing method.
        // showAlert("Info", "Search not yet implemented in backend.");
        // Re-load to ensure table is populated.
        loadDoctors();
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
            handleClear();
            showAlert("Success", "Doctor added successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add doctor: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateDoctor() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a doctor to update.");
            return;
        }
        if (!validateInput())
            return;

        selected.setFirstName(firstNameField.getText());
        selected.setLastName(lastNameField.getText());
        selected.setSpecialization(specializationField.getText());
        try {
            selected.setDepartmentId(Integer.parseInt(deptIdField.getText()));
        } catch (NumberFormatException e) {
            showAlert("Error", "Department ID must be a number.");
            return;
        }
        selected.setEmail(emailField.getText());
        selected.setPhone(phoneField.getText());

        try {
            hospitalService.updateDoctor(selected);
            loadDoctors();
            handleClear();
            showAlert("Success", "Doctor updated successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to update doctor: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteDoctor() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a doctor to delete.");
            return;
        }

        try {
            hospitalService.deleteDoctor(selected.getId());
            doctorList.remove(selected);
            handleClear();
            showAlert("Success", "Doctor deleted successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete doctor: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        firstNameField.clear();
        lastNameField.clear();
        specializationField.clear();
        deptIdField.clear();
        emailField.clear();
        phoneField.clear();
        doctorTable.getSelectionModel().clearSelection();
        if (searchField != null)
            searchField.clear();
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
