package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.HospitalService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
    private TableColumn<Patient, String> colAddress;
    @FXML
    private TableColumn<Patient, String> colCreatedAt;
    @FXML
    private TableColumn<Patient, Void> colActions;

    private HospitalService hospitalService;
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();

    public void initialize() {
        hospitalService = new HospitalService();
        setupTableColumns();
        setupActionColumn();
        loadPatients();
    }

    private void setupTableColumns() {
        // Bind columns to Patient model properties
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colDOB.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Custom cell value factory for created at date
        colCreatedAt.setCellValueFactory(cellData -> {
            LocalDateTime createdAt = cellData.getValue().getCreatedAt();
            if (createdAt == null) {
                return new SimpleStringProperty("-");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(createdAt.format(formatter));
        });
    }

    /**
     * Setup action column with Edit and Delete buttons
     */
    private void setupActionColumn() {
        // Find the Actions column by text if colActions is null
        TableColumn<Patient, Void> actionsCol = colActions;
        if (actionsCol == null) {
            for (TableColumn<Patient, ?> col : patientTable.getColumns()) {
                if ("Edit".equals(col.getText()) || "Actions".equals(col.getText())) {
                    actionsCol = (TableColumn<Patient, Void>) col;
                    break;
                }
            }
        }

        if (actionsCol != null) {
            actionsCol.setCellFactory(param -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Del");
                private final HBox pane = new HBox(5, editBtn, deleteBtn);

                {
                    pane.setAlignment(Pos.CENTER);
                    editBtn.setStyle(
                            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 3 8;");
                    deleteBtn.setStyle(
                            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 3 8;");

                    editBtn.setOnAction(event -> {
                        Patient patient = getTableView().getItems().get(getIndex());
                        handleEditPatient(patient);
                    });

                    deleteBtn.setOnAction(event -> {
                        Patient patient = getTableView().getItems().get(getIndex());
                        handleDeletePatient(patient);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(pane);
                    }
                }
            });
        }
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
        openPatientDialog(null);
    }

    /**
     * Open patient form dialog for editing
     */
    private void handleEditPatient(Patient patient) {
        openPatientDialog(patient);
    }

    /**
     * Open patient form dialog (modal)
     */
    private void openPatientDialog(Patient patient) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/hospital/view/PatientFormDialog.fxml"));
            Parent root = loader.load();

            // Get controller
            PatientFormDialogController controller = loader.getController();

            // Create stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle(patient == null ? "Add New Patient" : "Edit Patient");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(patientTable.getScene().getWindow());

            // Set scene
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Configure controller
            controller.setDialogStage(dialogStage);
            controller.setPatient(patient);

            // Show and wait
            dialogStage.showAndWait();

            // Refresh table if save was successful
            if (controller.isSaveSuccessful()) {
                loadPatients();
            }

        } catch (IOException e) {
            showAlert("Error", "Failed to open patient form:\n" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "Unexpected error:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle delete patient with confirmation
     */
    private void handleDeletePatient(Patient patient) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Patient");
        confirmAlert.setContentText(
                "Are you sure you want to delete " + patient.getFirstName() + " " + patient.getLastName() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                hospitalService.deletePatient(patient.getId());
                loadPatients();
                showAlert("Success", "Patient deleted successfully.");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete patient: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        patientTable.getSelectionModel().clearSelection();
        loadPatients();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
