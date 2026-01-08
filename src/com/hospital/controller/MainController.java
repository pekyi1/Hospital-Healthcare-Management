package com.hospital.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        loadView("/com/hospital/view/DashboardView.fxml");
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void showPatients() {
        loadView("/com/hospital/view/PatientView.fxml");
    }

    @FXML
    private void showDoctors() {
        loadView("/com/hospital/view/DoctorView.fxml");
    }

    @FXML
    private void showAppointments() {
        loadView("/com/hospital/view/AppointmentView.fxml");
    }

    @FXML
    private void showInventory() {
        loadView("/com/hospital/view/InventoryView.fxml");
    }

    @FXML
    private void showPrescriptions() {
        loadView("/com/hospital/view/PrescriptionView.fxml");
    }

    @FXML
    private void showPerformance() {
        loadView("/com/hospital/view/PerformanceView.fxml");
    }

    @FXML
    private void showFeedback() {
        loadView("/com/hospital/view/FeedbackView.fxml");
    }

    @FXML
    private javafx.scene.control.Button backButton;

    private java.util.Stack<String> navigationHistory = new java.util.Stack<>();
    private String currentView = "/com/hospital/view/DashboardView.fxml"; // Default start

    public void loadView(String fxmlPath) {
        // If we are navigating to a new view (not back), push current to history
        if (!fxmlPath.equals(currentView)) {
            navigationHistory.push(currentView);
            currentView = fxmlPath;
            backButton.setDisable(false);
        }

        loadViewInternal(fxmlPath);
    }

    private void loadViewInternal(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        if (!navigationHistory.isEmpty()) {
            String previousView = navigationHistory.pop();
            currentView = previousView; // Update current view tracking
            loadViewInternal(previousView);

            if (navigationHistory.isEmpty()) {
                backButton.setDisable(true);
            }
        }
    }
}
