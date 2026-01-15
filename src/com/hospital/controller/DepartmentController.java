package com.hospital.controller;

import com.hospital.model.Department;
import com.hospital.service.DepartmentService;
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
import java.util.List;
import java.util.Optional;

public class DepartmentController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Department> departmentTable;

    @FXML
    private TableColumn<Department, Integer> colId;
    @FXML
    private TableColumn<Department, String> colName;
    @FXML
    private TableColumn<Department, String> colLocation;
    @FXML
    private TableColumn<Department, Void> colActions;

    private DepartmentService departmentService;
    private ObservableList<Department> departmentList = FXCollections.observableArrayList();

    public void initialize() {
        departmentService = new DepartmentService();
        setupTableColumns();
        setupActionColumn();
        loadDepartments();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
    }

    private void setupActionColumn() {
        TableColumn<Department, Void> actionsCol = colActions;
        if (actionsCol == null) {
            for (TableColumn<Department, ?> col : departmentTable.getColumns()) {
                if ("Edit".equals(col.getText()) || "Actions".equals(col.getText())) {
                    actionsCol = (TableColumn<Department, Void>) col;
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
                        Department dept = getTableView().getItems().get(getIndex());
                        handleEditDepartment(dept);
                    });

                    deleteBtn.setOnAction(event -> {
                        Department dept = getTableView().getItems().get(getIndex());
                        handleDeleteDepartment(dept);
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

    private void loadDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            departmentList.setAll(departments);
            departmentTable.setItems(departmentList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load departments: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.isEmpty()) {
            loadDepartments();
            return;
        }
        // Basic search simulation or implementation if Service supports it.
        // DepartmentService doesn't explicitly have search, so we can filter locally or
        // just reload for now.
        // Or implement simple filter:
        String lower = keyword.toLowerCase();
        try {
            List<Department> all = departmentService.getAllDepartments();
            List<Department> filtered = all.stream()
                    .filter(d -> d.getName().toLowerCase().contains(lower)
                            || d.getLocation().toLowerCase().contains(lower))
                    .toList();
            departmentList.setAll(filtered);
            departmentTable.setItems(departmentList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddDepartment() {
        openDepartmentDialog(null);
    }

    private void handleEditDepartment(Department department) {
        openDepartmentDialog(department);
    }

    private void openDepartmentDialog(Department department) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/DepartmentFormDialog.fxml"));
            Parent root = loader.load();

            DepartmentFormDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(department == null ? "Add Department" : "Edit Department");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(departmentTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);
            controller.setDepartment(department);

            dialogStage.showAndWait();

            if (controller.isSaveSuccessful()) {
                loadDepartments();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load department form: " + e.getMessage());
        }
    }

    private void handleDeleteDepartment(Department department) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Department");
        confirmAlert.setContentText("Are you sure you want to delete the department: " +
                department.getName() + "?\n\nWarning: This may affect doctors assigned to this department.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                departmentService.deleteDepartment(department.getId());
                loadDepartments();
                showAlert("Success", "Department deleted successfully.");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete department: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        if (searchField != null) {
            searchField.clear();
        }
        departmentTable.getSelectionModel().clearSelection();
        loadDepartments();
    }

    @FXML
    private void handleRefresh() {
        loadDepartments();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
