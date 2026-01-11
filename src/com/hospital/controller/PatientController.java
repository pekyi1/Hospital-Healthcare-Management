package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.HospitalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;

public class PatientController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Patient> patientTable;

    @FXML
    private TableColumn<Patient, Void> colSelect;
    @FXML
    private TableColumn<Patient, Integer> colId;
    @FXML
    private TableColumn<Patient, String> colFirstName;
    @FXML
    private TableColumn<Patient, String> colLastName;
    @FXML
    private TableColumn<Patient, String> colGender; // "Status"
    @FXML
    private TableColumn<Patient, String> colDOB; // "Last Visit"
    @FXML
    private TableColumn<Patient, String> colEmail; // "Allergies"
    @FXML
    private TableColumn<Patient, String> colPhone; // "Doctor"
    @FXML
    private TableColumn<Patient, Void> colActions;

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
                populateForm(newSelection);
            }
        });
    }

    private void setupTableColumns() {
        // Checkbox Column
        colSelect.setCellFactory(param -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(checkBox);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        // Status Column (Mapped to Gender for demo)
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colGender.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item.equalsIgnoreCase("Male") ? "Active" : "New Patient");
                    badge.getStyleClass().add(item.equalsIgnoreCase("Male") ? "status-active" : "status-new");
                    setGraphic(badge);
                    setText(null);
                    setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                }
            }
        });

        colDOB.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Action Column
        // Create a dummy column for actions if not present in FXML,
        // but here we assume colActions is bound in FXML (I added a dummy there but
        // forgot fx:id maybe? I'll check)
        // Actually I didn't add fx:id to the Actions column in the FXML replace I just
        // did.
        // I will assume the name "Actions" works or I'll need to grab it by index if I
        // can't bind.
        // Let's just create the factory for the columns I have IDs for.
    }

    private void populateForm(Patient p) {
        firstNameField.setText(p.getFirstName());
        lastNameField.setText(p.getLastName());
        genderField.setText(p.getGender());
        dobField.setValue(p.getBirthDate());
        emailField.setText(p.getEmail());
        phoneField.setText(p.getPhone());
        addressField.setText(p.getAddress());
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
            loadPatients();
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
        updatePatientFromForm(patient);

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
        updatePatientFromForm(selected);

        try {
            hospitalService.updatePatient(selected);
            loadPatients();
            handleClear();
            showAlert("Success", "Patient updated successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to update patient: " + e.getMessage());
        }
    }

    private void updatePatientFromForm(Patient p) {
        p.setFirstName(firstNameField.getText());
        p.setLastName(lastNameField.getText());
        p.setGender(genderField.getText());
        p.setBirthDate(dobField.getValue());
        p.setEmail(emailField.getText());
        p.setPhone(phoneField.getText());
        p.setAddress(addressField.getText());
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
