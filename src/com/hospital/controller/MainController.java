package com.hospital.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Stack;
import java.util.List;
import java.util.Arrays;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnPatients;
    @FXML
    private Button btnDoctors;
    @FXML
    private Button btnAppointments;
    @FXML
    private Button btnInventory;
    @FXML
    private Button btnPrescriptions;
    @FXML
    private Button btnPerformance;
    @FXML
    private Button btnFeedback;
    @FXML
    private Button btnDepartments;

    @FXML
    private Button backButton;

    // Chrome Tabs
    @FXML
    private Button btnTabAdmin;
    @FXML
    private Button btnTabDoctor;
    @FXML
    private Button btnTabPatient;

    @FXML
    private Label lblWelcome;

    private static MainController instance;
    private String currentRole = "Admin"; // Track current role

    private Stack<String> navigationHistory = new Stack<>();
    private String currentView = "/com/hospital/view/DashboardView.fxml"; // Default start

    public static MainController getInstance() {
        return instance;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    @FXML
    public void initialize() {
        instance = this;
        // Default to Admin view
        switchRoleAdmin();
    }

    @FXML
    private void showDashboard() {
        loadView("/com/hospital/view/DashboardView.fxml", btnDashboard);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void showPatients() {
        loadView("/com/hospital/view/PatientView.fxml", btnPatients);
    }

    @FXML
    private void showDoctors() {
        loadView("/com/hospital/view/DoctorView.fxml", btnDoctors);
    }

    @FXML
    private void showAppointments() {
        loadView("/com/hospital/view/AppointmentView.fxml", btnAppointments);
    }

    @FXML
    private void showDepartments() {
        loadView("/com/hospital/view/DepartmentView.fxml", btnDepartments);
    }

    @FXML
    private void showInventory() {
        loadView("/com/hospital/view/InventoryView.fxml", btnInventory);
    }

    @FXML
    private void showPrescriptions() {
        loadView("/com/hospital/view/PrescriptionView.fxml", btnPrescriptions);
    }

    @FXML
    private void showPerformance() {
        loadView("/com/hospital/view/PerformanceView.fxml", btnPerformance);
    }

    @FXML
    private void showFeedback() {
        loadView("/com/hospital/view/FeedbackView.fxml", btnFeedback);
    }

    public void loadView(String fxmlPath) {
        loadView(fxmlPath, null);
    }

    public void loadView(String fxmlPath, Button activeButton) {
        // If we are navigating to a new view (not back), push current to history
        if (!fxmlPath.equals(currentView)) {
            navigationHistory.push(currentView);
            currentView = fxmlPath;
            if (backButton != null)
                backButton.setDisable(false);
        }

        if (activeButton != null) {
            updateActiveButton(activeButton);
        }

        loadViewInternal(fxmlPath);
    }

    private void updateActiveButton(Button activeButton) {
        // Reset all buttons
        List<Button> buttons = Arrays.asList(
                btnDashboard, btnPatients, btnDoctors, btnAppointments,
                btnDepartments, btnInventory, btnPrescriptions, btnPerformance, btnFeedback);

        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("active");
                // btn.getStyleClass().remove("sidebar-button-selected"); // Legacy
            }
        }

        // Set active
        if (activeButton != null) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void loadViewInternal(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            // Animation
            FadeTransition fade = new FadeTransition(Duration.millis(300), view);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

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
    // --- Role Switching Logic ---

    @FXML
    private void switchRoleAdmin() {
        setRole("Admin");
        showDashboard();
    }

    @FXML
    private void switchRoleDoctor() {
        setRole("Doctor");
        showDashboard();
    }

    @FXML
    private void switchRolePatient() {
        setRole("Patient");
        // Patient might not have a dashboard, or a different one.
        // For now, load Appointments as default for Patient?
        // Or just let them see the restricted dashboard.
        showAppointments(); // Default for patient
    }

    private void setRole(String role) {
        this.currentRole = role; // Store current role

        // Welcome Message
        if (lblWelcome != null) {
            lblWelcome.setText("Welcome back, " + role + " \uD83D\uDC4B"); // Wave emoji
        }

        // 1. Update Tab Styles
        updateTabStyle(btnTabAdmin, role.equals("Admin"));
        updateTabStyle(btnTabDoctor, role.equals("Doctor"));
        updateTabStyle(btnTabPatient, role.equals("Patient"));

        // 2. Configure Sidebar based on Role
        boolean isAdmin = role.equals("Admin");
        boolean isDoctor = role.equals("Doctor");
        boolean isPatient = role.equals("Patient");

        // Admin sees everything
        // Doctor sees: Dashboard, Patients, Appointments, Prescriptions, Feedback
        // Patient sees: Appointments, Doctors, Prescriptions

        setSidebarButtonVisible(btnDashboard, isAdmin || isDoctor);
        setSidebarButtonVisible(btnPatients, isAdmin || isDoctor);
        setSidebarButtonVisible(btnDoctors, isAdmin || isPatient); // Patient needs to find doctors
        setSidebarButtonVisible(btnAppointments, true); // Everyone needs appointments
        setSidebarButtonVisible(btnDepartments, isAdmin);
        setSidebarButtonVisible(btnInventory, isAdmin);
        setSidebarButtonVisible(btnPrescriptions, true); // Everyone? (Patient views theirs)
        setSidebarButtonVisible(btnPerformance, isAdmin);
        setSidebarButtonVisible(btnFeedback, isAdmin || isDoctor); // Maybe patient too?
    }

    private void updateTabStyle(Button btn, boolean isActive) {
        if (btn == null)
            return;
        btn.getStyleClass().remove("active");
        if (isActive) {
            btn.getStyleClass().add("active");
        }
    }

    private void setSidebarButtonVisible(Button btn, boolean visible) {
        if (btn == null)
            return;
        btn.setVisible(visible);
        btn.setManaged(visible); // Collapses space if hidden
    }
}
