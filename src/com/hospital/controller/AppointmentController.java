package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AppointmentController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Appointment> appointmentTable;

    @FXML
    private TableColumn<Appointment, String> colPatientName;
    @FXML
    private TableColumn<Appointment, String> colDoctorName;
    @FXML
    private TableColumn<Appointment, LocalDateTime> colDate;
    @FXML
    private TableColumn<Appointment, String> colStatus;
    @FXML
    private TableColumn<Appointment, String> colNotes;
    @FXML
    private TableColumn<Appointment, Void> colActions;

    private HospitalService hospitalService;
    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

    // Cache for patient and doctor names
    private Map<Integer, String> patientNameCache = new HashMap<>();
    private Map<Integer, String> doctorNameCache = new HashMap<>();

    public void initialize() {
        hospitalService = new HospitalService();
        setupTableColumns();
        setupActionColumn();
        loadAppointments();
    }

    private void setupTableColumns() {
        // Bind columns to Appointment model properties
        colDate.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Custom cell value factories for patient and doctor names
        colPatientName.setCellValueFactory(cellData -> {
            int patientId = cellData.getValue().getPatientId();
            String name = getPatientName(patientId);
            return new SimpleStringProperty(name);
        });

        colDoctorName.setCellValueFactory(cellData -> {
            int doctorId = cellData.getValue().getDoctorId();
            String name = getDoctorName(doctorId);
            return new SimpleStringProperty(name);
        });
    }

    private String getPatientName(int patientId) {
        if (patientNameCache.containsKey(patientId)) {
            return patientNameCache.get(patientId);
        }
        try {
            Patient patient = hospitalService.getPatientById(patientId);
            if (patient != null) {
                String name = patient.getFirstName() + " " + patient.getLastName();
                patientNameCache.put(patientId, name);
                return name;
            }
        } catch (SQLException e) {
            // Return ID as fallback
        }
        return "Patient #" + patientId;
    }

    private String getDoctorName(int doctorId) {
        if (doctorNameCache.containsKey(doctorId)) {
            return doctorNameCache.get(doctorId);
        }
        try {
            Doctor doctor = hospitalService.getDoctorById(doctorId);
            if (doctor != null) {
                String name = "Dr. " + doctor.getFirstName() + " " + doctor.getLastName();
                doctorNameCache.put(doctorId, name);
                return name;
            }
        } catch (SQLException e) {
            // Return ID as fallback
        }
        return "Doctor #" + doctorId;
    }

    /**
     * Setup action column with Edit and Delete buttons
     */
    private void setupActionColumn() {
        // Find the Actions column by text if colActions is null
        TableColumn<Appointment, Void> actionsCol = colActions;
        if (actionsCol == null) {
            for (TableColumn<Appointment, ?> col : appointmentTable.getColumns()) {
                if ("Edit".equals(col.getText()) || "Actions".equals(col.getText())) {
                    actionsCol = (TableColumn<Appointment, Void>) col;
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
                        Appointment appointment = getTableView().getItems().get(getIndex());
                        handleEditAppointment(appointment);
                    });

                    deleteBtn.setOnAction(event -> {
                        Appointment appointment = getTableView().getItems().get(getIndex());
                        handleDeleteAppointment(appointment);
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

    private void loadAppointments() {
        // Clear caches to refresh names
        patientNameCache.clear();
        doctorNameCache.clear();

        try {
            List<Appointment> appointments = hospitalService.getAllAppointments();
            appointmentList.setAll(appointments);
            appointmentTable.setItems(appointmentList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appointments: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.isEmpty()) {
            loadAppointments();
            return;
        }
        // For now, reload all - can implement search in service later
        loadAppointments();
    }

    @FXML
    private void handleAddAppointment() {
        openAppointmentDialog(null);
    }

    private void handleEditAppointment(Appointment appointment) {
        openAppointmentDialog(appointment);
    }

    private void openAppointmentDialog(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/AppointmentFormDialog.fxml"));
            Parent root = loader.load();

            AppointmentFormDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(appointment == null ? "Add Appointment" : "Edit Appointment");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(appointmentTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);
            controller.setAppointment(appointment);

            dialogStage.showAndWait();

            if (controller.isSaveSuccessful()) {
                loadAppointments();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load appointment form: " + e.getMessage());
        }
    }

    private void handleDeleteAppointment(Appointment appointment) {
        String patientName = getPatientName(appointment.getPatientId());
        String doctorName = getDoctorName(appointment.getDoctorId());

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Appointment");
        confirmAlert.setContentText("Are you sure you want to delete this appointment?\n\n" +
                "Patient: " + patientName + "\n" +
                "Doctor: " + doctorName);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                hospitalService.cancelAppointment(appointment.getId());
                loadAppointments();
                showAlert("Success", "Appointment deleted successfully.");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete appointment: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        if (searchField != null) {
            searchField.clear();
        }
        appointmentTable.getSelectionModel().clearSelection();
        loadAppointments();
    }

    @FXML
    private void handleRefresh() {
        loadAppointments();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
