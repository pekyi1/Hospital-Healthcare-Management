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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class InventoryController {

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

    private HospitalService hospitalService;
    private ObservableList<MedicalInventory> inventoryList = FXCollections.observableArrayList();

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

        loadInventory();
    }

    private void loadInventory() {
        try {
            inventoryList.setAll(hospitalService.getAllInventoryItems());
            inventoryTable.setItems(inventoryList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load inventory: " + e.getMessage());
        }
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
        nameField.setPromptText("Item Name");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        TextField priceField = new TextField();
        priceField.setPromptText("Unit Price");

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
                    String name = nameField.getText();
                    String category = categoryField.getText();
                    int qty = Integer.parseInt(quantityField.getText());
                    BigDecimal price = new BigDecimal(priceField.getText());

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
