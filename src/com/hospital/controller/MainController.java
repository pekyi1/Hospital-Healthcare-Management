package com.hospital.controller;

import com.hospital.model.User;
import com.hospital.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.stage.Stage;

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

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblRole;

    private static MainController instance;

    private Stack<String> navigationHistory = new Stack<>();
    private String currentView = "/com/hospital/view/DashboardView.fxml"; // Default start

    public static MainController getInstance() {
        return instance;
    }

    /**
     * Gets the current user's role from the session.
     */
    public String getCurrentRole() {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null ? currentUser.getRole() : "Guest";
    }

    @FXML
    public void initialize() {
        instance = this;

        // Set up UI based on logged-in user
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            setupUIForRole(currentUser);
        }
    }

    /**
     * Configures the UI based on the logged-in user's role.
     * 
     * Role Privileges:
     * - Admin: Full access to all modules
     * - Doctor: Dashboard, Patients, Doctors, Appointments, Prescriptions, Feedback
     * - Patient: Feedback, Prescriptions (view only)
     */
    private void setupUIForRole(User user) {
        String role = user.getRole();

        // Update welcome message
        if (lblWelcome != null) {
            switch (role) {
                case "Admin":
                    lblWelcome.setText("Welcome, Administrator 👋");
                    break;
                case "Doctor":
                    lblWelcome.setText("Welcome, Doctor 👋");
                    break;
                case "Patient":
                    lblWelcome.setText("Welcome, Patient 👋");
                    break;
                default:
                    lblWelcome.setText("Welcome 👋");
            }
        }

        // Update role label
        if (lblRole != null) {
            switch (role) {
                case "Admin":
                    lblRole.setText("Full System Access");
                    break;
                case "Doctor":
                    lblRole.setText("Clinical Access");
                    break;
                case "Patient":
                    lblRole.setText("Limited View");
                    break;
                default:
                    lblRole.setText(role);
            }
        }

        // Configure sidebar visibility based on role
        boolean isAdmin = "Admin".equals(role);
        boolean isDoctor = "Doctor".equals(role);
        boolean isPatient = "Patient".equals(role);

        // Admin: ALL features
        // Doctor: Dashboard, Patients, Doctors, Appointments, Prescriptions, Feedback
        // Patient: Feedback, Prescriptions ONLY

        setSidebarButtonVisible(btnDashboard, isAdmin || isDoctor);
        setSidebarButtonVisible(btnPatients, isAdmin || isDoctor);
        setSidebarButtonVisible(btnDoctors, isAdmin || isDoctor);
        setSidebarButtonVisible(btnAppointments, isAdmin || isDoctor);
        setSidebarButtonVisible(btnDepartments, isAdmin);
        setSidebarButtonVisible(btnInventory, isAdmin);
        setSidebarButtonVisible(btnPrescriptions, true); // All roles can view prescriptions
        setSidebarButtonVisible(btnPerformance, isAdmin);
        setSidebarButtonVisible(btnFeedback, true); // All roles can access feedback

        // Navigate to appropriate default view based on role
        if (isAdmin) {
            showDashboard();
        } else if (isDoctor) {
            showDashboard();
        } else if (isPatient) {
            // Patients start at Feedback
            showFeedback();
        }
    }

    @FXML
    private void showDashboard() {
        loadView("/com/hospital/view/DashboardView.fxml", btnDashboard);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    /**
     * Signs out the current user and returns to the login screen.
     */
    @FXML
    private void handleSignOut() {
        // Clear the session
        SessionManager.logout();

        // Navigate back to login screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();

            // Preserve current window dimensions
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMaximized = stage.isMaximized();

            Scene scene = new Scene(root, width, height);

            // Fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), contentArea);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(scene);
                stage.setTitle("Hospital Management System - Login");

                // Restore maximized state
                if (isMaximized) {
                    stage.setMaximized(true);
                }
            });
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void setSidebarButtonVisible(Button btn, boolean visible) {
        if (btn == null)
            return;
        btn.setVisible(visible);
        btn.setManaged(visible); // Collapses space if hidden
    }
}
