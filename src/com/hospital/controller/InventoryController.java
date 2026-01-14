package com.hospital.controller;

import com.hospital.model.MedicalInventory;
import com.hospital.service.HospitalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoryController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<MedicalInventory> inventoryTable;
    @FXML
    private TableColumn<MedicalInventory, Integer> idColumn;
    @FXML
    private TableColumn<MedicalInventory, String> nameColumn;
    @FXML
    private TableColumn<MedicalInventory, String> categoryColumn;
    @FXML
    private TableColumn<MedicalInventory, Integer> quantityColumn;
    @FXML
    private TableColumn<MedicalInventory, BigDecimal> priceColumn;
    @FXML
    private TableColumn<MedicalInventory, String> updatedColumn;
    @FXML
    private TableColumn<MedicalInventory, Void> actionsColumn;

    private HospitalService hospitalService;
    private ObservableList<MedicalInventory> inventoryList = FXCollections.observableArrayList();
    private List<MedicalInventory> allItems;

    public InventoryController() {
        this.hospitalService = new HospitalService();
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        updatedColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));

        setupActionsColumn();
        loadInventory();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle(
                        "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand;");
                deleteBtn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand;");

                editBtn.setOnAction(event -> {
                    MedicalInventory item = getTableView().getItems().get(getIndex());
                    handleEditItem(item);
                });

                deleteBtn.setOnAction(event -> {
                    MedicalInventory item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item);
                });
            }

            @Override
            protected void updateItem(Void unused, boolean empty) {
                super.updateItem(unused, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void loadInventory() {
        try {
            allItems = hospitalService.getAllInventoryItems();
            inventoryList.setAll(allItems);
            inventoryTable.setItems(inventoryList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load inventory: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            inventoryList.setAll(allItems);
        } else {
            List<MedicalInventory> filtered = allItems.stream()
                    .filter(item -> item.getItemName().toLowerCase().contains(keyword)
                            || item.getCategory().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            inventoryList.setAll(filtered);
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadInventory();
    }

    @FXML
    private void handleAddItem() {
        Dialog<MedicalInventory> dialog = createItemDialog(null);
        Optional<MedicalInventory> result = dialog.showAndWait();

        result.ifPresent(item -> {
            try {
                hospitalService.addInventoryItem(item);
                loadInventory();
            } catch (SQLException e) {
                showAlert("Error", "Failed to add item: " + e.getMessage());
            }
        });
    }

    private void handleEditItem(MedicalInventory item) {
        Dialog<MedicalInventory> dialog = createItemDialog(item);
        Optional<MedicalInventory> result = dialog.showAndWait();

        result.ifPresent(updatedItem -> {
            try {
                hospitalService.updateInventoryItem(updatedItem);
                loadInventory();
            } catch (SQLException e) {
                showAlert("Error", "Failed to update item: " + e.getMessage());
            }
        });
    }

    private void handleDeleteItem(MedicalInventory item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Inventory Item");
        confirm.setContentText("Are you sure you want to delete \"" + item.getItemName() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                hospitalService.deleteInventoryItem(item.getId());
                loadInventory();
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete item: " + e.getMessage());
            }
        }
    }

    private Dialog<MedicalInventory> createItemDialog(MedicalInventory existingItem) {
        Dialog<MedicalInventory> dialog = new Dialog<>();
        dialog.setTitle(existingItem == null ? "Add Inventory Item" : "Edit Inventory Item");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Paracetamol, Amoxicillin");
        TextField categoryField = new TextField();
        categoryField.setPromptText("e.g., Medicine, Antibiotic, Supply");
        TextField quantityField = new TextField();
        quantityField.setPromptText("e.g., 500");
        TextField priceField = new TextField();
        priceField.setPromptText("e.g., 5.00");

        if (existingItem != null) {
            nameField.setText(existingItem.getItemName());
            categoryField.setText(existingItem.getCategory());
            quantityField.setText(String.valueOf(existingItem.getQuantity()));
            priceField.setText(existingItem.getUnitPrice().toString());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String category = categoryField.getText().trim();

                    if (name.isEmpty() || category.isEmpty()) {
                        showAlert("Validation Error", "Name and Category are required.");
                        return null;
                    }

                    int qty = Integer.parseInt(quantityField.getText().trim());
                    BigDecimal price = new BigDecimal(priceField.getText().trim());

                    if (existingItem != null) {
                        existingItem.setItemName(name);
                        existingItem.setCategory(category);
                        existingItem.setQuantity(qty);
                        existingItem.setUnitPrice(price);
                        return existingItem;
                    } else {
                        return new MedicalInventory(0, name, category, qty, price);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Validation Error", "Invalid quantity or price format.");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
