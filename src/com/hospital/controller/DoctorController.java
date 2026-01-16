package com.hospital.controller;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.service.DepartmentService;
import com.hospital.service.DoctorService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DoctorController {

    @FXML
    private TextField searchField;
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
    private TableColumn<Doctor, String> colDepartment;
    @FXML
    private TableColumn<Doctor, String> colEmail;
    @FXML
    private TableColumn<Doctor, String> colPhone;
    @FXML
    private TableColumn<Doctor, String> colCreatedAt;
    @FXML
    private TableColumn<Doctor, Void> colActions;

    private DoctorService doctorService;
    private DepartmentService departmentService;
    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();

    // Cache for department names
    private final Map<Integer, String> departmentNameCache = new HashMap<>();

    public void initialize() {
        doctorService = new DoctorService();
        departmentService = new DepartmentService();
        setupTableColumns();
        setupActionColumn();
        loadDoctors();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Custom cell value factory for department name
        colDepartment.setCellValueFactory(cellData -> {
            int deptId = cellData.getValue().getDepartmentId();
            String name = getDepartmentName(deptId);
            return new SimpleStringProperty(name);
        });

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

    private String getDepartmentName(int deptId) {
        if (deptId == 0)
            return "-";
        if (departmentNameCache.containsKey(deptId)) {
            return departmentNameCache.get(deptId);
        }
        try {
            Department dept = departmentService.getDepartmentById(deptId);
            if (dept != null) {
                departmentNameCache.put(deptId, dept.getName());
                return dept.getName();
            }
        } catch (SQLException e) {
            // Return ID as fallback
        }
        return "Dept #" + deptId;
    }

    private void setupActionColumn() {
        TableColumn<Doctor, Void> actionsCol = colActions;
        if (actionsCol == null) {
            for (TableColumn<Doctor, ?> col : doctorTable.getColumns()) {
                if ("Edit".equals(col.getText()) || "Actions".equals(col.getText())) {
                    actionsCol = (TableColumn<Doctor, Void>) col;
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
                        Doctor doctor = getTableView().getItems().get(getIndex());
                        handleEditDoctor(doctor);
                    });

                    deleteBtn.setOnAction(event -> {
                        Doctor doctor = getTableView().getItems().get(getIndex());
                        handleDeleteDoctor(doctor);
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

    private void loadDoctors() {
        // Clear cache to refresh department names
        departmentNameCache.clear();

        try {
            List<Doctor> doctors = doctorService.getAllDoctors();
            doctorList.setAll(doctors);
            doctorTable.setItems(doctorList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load doctors: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            loadDoctors();
            return;
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        try {
            List<Doctor> allDoctors = doctorService.getAllDoctors();
            List<Doctor> filtered = allDoctors.stream()
                    .filter(d -> d.getFirstName().toLowerCase().contains(lowerKeyword) ||
                            d.getLastName().toLowerCase().contains(lowerKeyword) ||
                            (d.getSpecialization() != null
                                    && d.getSpecialization().toLowerCase().contains(lowerKeyword))
                            ||
                            (d.getEmail() != null && d.getEmail().toLowerCase().contains(lowerKeyword)))
                    .toList();

            doctorList.setAll(filtered);
            doctorTable.setItems(doctorList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to search doctors: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddDoctor() {
        openDoctorDialog(null);
    }

    private void handleEditDoctor(Doctor doctor) {
        openDoctorDialog(doctor);
    }

    private void openDoctorDialog(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/DoctorFormDialog.fxml"));
            Parent root = loader.load();

            DoctorFormDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(doctor == null ? "Add Doctor" : "Edit Doctor");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(doctorTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);
            controller.setDoctor(doctor);

            dialogStage.showAndWait();

            if (controller.isSaveSuccessful()) {
                loadDoctors();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load doctor form: " + e.getMessage());
        }
    }

    private void handleDeleteDoctor(Doctor doctor) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Doctor");
        confirmAlert.setContentText("Are you sure you want to delete Dr. " +
                doctor.getFirstName() + " " + doctor.getLastName() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                doctorService.deleteDoctor(doctor.getId());
                loadDoctors();
                showAlert("Success", "Doctor deleted successfully.");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete doctor: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        if (searchField != null) {
            searchField.clear();
        }
        doctorTable.getSelectionModel().clearSelection();
        loadDoctors();
    }

    @FXML
    private void handleRefresh() {
        loadDoctors();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
