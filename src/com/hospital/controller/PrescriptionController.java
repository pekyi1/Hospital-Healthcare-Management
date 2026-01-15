package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.HospitalService;
import com.hospital.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * View model combining Prescription header with its first item for display
 */
class PrescriptionDisplayItem {
    private int prescriptionId;
    private int patientId;
    private int doctorId;
    private Timestamp prescriptionDate;
    private int inventoryId;
    private String medicineName;
    private int quantity;
    private String dosageInstructions;

    public PrescriptionDisplayItem(Prescription p, PrescriptionItem item) {
        this.prescriptionId = p.getId();
        this.patientId = p.getPatientId();
        this.doctorId = p.getDoctorId();
        this.prescriptionDate = p.getPrescriptionDate();
        if (item != null) {
            this.inventoryId = item.getInventoryId();
            this.medicineName = item.getMedicineName();
            this.quantity = item.getQuantity();
            this.dosageInstructions = item.getDosageInstructions();
        }
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public Timestamp getPrescriptionDate() {
        return prescriptionDate;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public String getMedicineName() {
        return medicineName != null ? medicineName : "-";
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDosageInstructions() {
        return dosageInstructions != null ? dosageInstructions : "-";
    }
}

public class PrescriptionController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<PrescriptionDisplayItem> prescriptionTable;
    @FXML
    private Button btnAddPrescription;

    @FXML
    private TableColumn<PrescriptionDisplayItem, String> colPatientName;
    @FXML
    private TableColumn<PrescriptionDisplayItem, String> colDoctorName;
    @FXML
    private TableColumn<PrescriptionDisplayItem, String> colMedicine;
    @FXML
    private TableColumn<PrescriptionDisplayItem, Integer> colQuantity;
    @FXML
    private TableColumn<PrescriptionDisplayItem, String> colDosage;
    @FXML
    private TableColumn<PrescriptionDisplayItem, String> colDate;
    @FXML
    private TableColumn<PrescriptionDisplayItem, Void> colActions;

    private HospitalService hospitalService;
    private ObservableList<PrescriptionDisplayItem> prescriptionList = FXCollections.observableArrayList();

    // Cache for patient and doctor names
    private Map<Integer, String> patientNameCache = new HashMap<>();
    private Map<Integer, String> doctorNameCache = new HashMap<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void initialize() {
        hospitalService = new HospitalService();
        setupTableColumns();

        // Check role and configure UI
        boolean isPatient = SessionManager.isPatient();

        if (isPatient) {
            // Patients can only view prescriptions, not add/edit/delete
            if (btnAddPrescription != null) {
                btnAddPrescription.setVisible(false);
                btnAddPrescription.setManaged(false);
            }
            // Don't setup action column for patients (no edit/delete)
        } else {
            // Admins and Doctors can add/edit/delete
            setupActionColumn();
        }

        loadPrescriptions();
    }

    private void setupTableColumns() {
        // Patient name
        colPatientName.setCellValueFactory(cellData -> {
            int patientId = cellData.getValue().getPatientId();
            String name = getPatientName(patientId);
            return new SimpleStringProperty(name);
        });

        // Doctor name
        colDoctorName.setCellValueFactory(cellData -> {
            int doctorId = cellData.getValue().getDoctorId();
            String name = getDoctorName(doctorId);
            return new SimpleStringProperty(name);
        });

        // Medicine name
        colMedicine.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMedicineName()));

        // Quantity
        colQuantity.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());

        // Dosage instructions
        colDosage
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDosageInstructions()));

        // Date
        colDate.setCellValueFactory(cellData -> {
            Timestamp date = cellData.getValue().getPrescriptionDate();
            if (date != null) {
                return new SimpleStringProperty(date.toLocalDateTime().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("-");
        });
    }

    private String getPatientName(int patientId) {
        if (patientId == 0)
            return "-";
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
        if (doctorId == 0)
            return "-";
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

    private void setupActionColumn() {
        if (colActions != null) {
            colActions.setCellFactory(param -> new TableCell<>() {
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
                        PrescriptionDisplayItem item = getTableView().getItems().get(getIndex());
                        handleEditPrescription(item);
                    });

                    deleteBtn.setOnAction(event -> {
                        PrescriptionDisplayItem item = getTableView().getItems().get(getIndex());
                        handleDeletePrescription(item);
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

    private void loadPrescriptions() {
        // Clear cache
        patientNameCache.clear();
        doctorNameCache.clear();
        prescriptionList.clear();

        try {
            List<Prescription> prescriptions = hospitalService.getAllPrescriptionsWithItems();
            for (Prescription p : prescriptions) {
                if (p.getItems() != null && !p.getItems().isEmpty()) {
                    // Create a display row for each item in the prescription
                    for (PrescriptionItem item : p.getItems()) {
                        prescriptionList.add(new PrescriptionDisplayItem(p, item));
                    }
                } else {
                    // Prescription with no items
                    prescriptionList.add(new PrescriptionDisplayItem(p, null));
                }
            }
            prescriptionTable.setItems(prescriptionList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load prescriptions: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            loadPrescriptions();
            return;
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        try {
            List<Prescription> allPrescriptions = hospitalService.getAllPrescriptionsWithItems();
            prescriptionList.clear();

            for (Prescription p : allPrescriptions) {
                String patientName = getPatientName(p.getPatientId()).toLowerCase();
                String doctorName = getDoctorName(p.getDoctorId()).toLowerCase();

                if (p.getItems() != null && !p.getItems().isEmpty()) {
                    for (PrescriptionItem item : p.getItems()) {
                        String medicineName = item.getMedicineName() != null ? item.getMedicineName().toLowerCase()
                                : "";
                        String dosage = item.getDosageInstructions() != null
                                ? item.getDosageInstructions().toLowerCase()
                                : "";

                        if (patientName.contains(lowerKeyword) ||
                                doctorName.contains(lowerKeyword) ||
                                medicineName.contains(lowerKeyword) ||
                                dosage.contains(lowerKeyword)) {
                            prescriptionList.add(new PrescriptionDisplayItem(p, item));
                        }
                    }
                }
            }

            prescriptionTable.setItems(prescriptionList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to search prescriptions: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddPrescription() {
        openPrescriptionDialog(null);
    }

    private void handleEditPrescription(PrescriptionDisplayItem displayItem) {
        // For editing, we pass the prescription ID so the form can load it
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hospital/view/PrescriptionFormDialog.fxml"));
            Parent root = loader.load();

            PrescriptionFormDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Prescription");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(prescriptionTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);
            controller.setPrescriptionForEdit(displayItem.getPrescriptionId(), displayItem.getInventoryId());

            dialogStage.showAndWait();

            if (controller.isSaveSuccessful()) {
                loadPrescriptions();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load prescription form: " + e.getMessage());
        }
    }

    private void openPrescriptionDialog(Prescription prescription) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hospital/view/PrescriptionFormDialog.fxml"));
            Parent root = loader.load();

            PrescriptionFormDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Prescription");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(prescriptionTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);
            controller.setPrescription(null);

            dialogStage.showAndWait();

            if (controller.isSaveSuccessful()) {
                loadPrescriptions();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load prescription form: " + e.getMessage());
        }
    }

    private void handleDeletePrescription(PrescriptionDisplayItem displayItem) {
        String patientName = getPatientName(displayItem.getPatientId());

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Prescription");
        confirmAlert.setContentText("Are you sure you want to delete the prescription for " + patientName +
                " (" + displayItem.getMedicineName() + ")?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                hospitalService.deletePrescription(displayItem.getPrescriptionId());
                loadPrescriptions();
                showAlert("Success", "Prescription deleted successfully.");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete prescription: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        if (searchField != null) {
            searchField.clear();
        }
        prescriptionTable.getSelectionModel().clearSelection();
        loadPrescriptions();
    }

    @FXML
    private void handleRefresh() {
        loadPrescriptions();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
