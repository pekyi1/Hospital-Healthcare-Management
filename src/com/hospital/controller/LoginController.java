package com.hospital.controller;

import com.hospital.model.User;
import com.hospital.dao.UserDAO;
import com.hospital.util.PasswordUtil;
import com.hospital.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller for the Login view.
 * Handles role-based login with credentials for Admin/Doctor.
 */
public class LoginController {

    @FXML
    private ScrollPane rootPane;

    @FXML
    private VBox roleSelectionPane;

    @FXML
    private VBox credentialsPane;

    @FXML
    private Label lblSelectedRole;

    @FXML
    private Label lblError;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    private String selectedRole = "";
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Clear any previous session
        SessionManager.logout();

        // Set up enter key on password field
        if (txtPassword != null) {
            txtPassword.setOnAction(e -> handleLogin());
        }
    }

    /**
     * Admin button clicked - show credentials form
     */
    @FXML
    private void handleAdminClick() {
        selectedRole = "Admin";
        showCredentialsForm("Admin Login");
    }

    /**
     * Doctor button clicked - show credentials form
     */
    @FXML
    private void handleDoctorClick() {
        selectedRole = "Doctor";
        showCredentialsForm("Doctor Login");
    }

    /**
     * Patient button clicked - go directly to patient view
     */
    @FXML
    private void handlePatientLogin() {
        // Create a session user with Patient role (no credentials needed)
        User user = new User();
        user.setUsername("Patient");
        user.setRole("Patient");
        user.setActive(true);

        SessionManager.login(user);
        navigateToMainApp();
    }

    /**
     * Shows the credentials form for Admin/Doctor login
     */
    private void showCredentialsForm(String title) {
        lblSelectedRole.setText(title);
        hideError();

        // Clear previous input
        txtUsername.clear();
        txtPassword.clear();

        // Transition from role selection to credentials
        roleSelectionPane.setVisible(false);
        roleSelectionPane.setManaged(false);

        credentialsPane.setVisible(true);
        credentialsPane.setManaged(true);

        // Focus on username field
        txtUsername.requestFocus();
    }

    /**
     * Back button - return to role selection
     */
    @FXML
    private void handleBackToRoles() {
        hideError();

        credentialsPane.setVisible(false);
        credentialsPane.setManaged(false);

        roleSelectionPane.setVisible(true);
        roleSelectionPane.setManaged(true);
    }

    /**
     * Login button clicked - validate credentials
     */
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Find user in database
        User user = userDAO.findByUsername(username);

        if (user == null) {
            showError("Invalid username or password");
            return;
        }

        // Check if user is active
        if (!user.isActive()) {
            showError("This account has been deactivated");
            return;
        }

        // Check if role matches
        if (!user.getRole().equals(selectedRole)) {
            showError("Invalid credentials for " + selectedRole + " login");
            return;
        }

        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            showError("Invalid username or password");
            return;
        }

        // Success - update last login and create session
        userDAO.updateLastLogin(user.getId());
        SessionManager.login(user);
        navigateToMainApp();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void navigateToMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();

            // Preserve current window dimensions
            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean isMaximized = stage.isMaximized();

            Scene scene = new Scene(root, width, height);

            // Fade transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            stage.setScene(scene);
            stage.setTitle("Hospital Management System - " + SessionManager.getCurrentUser().getRole());

            // Restore maximized state if it was maximized
            if (isMaximized) {
                stage.setMaximized(true);
            }

            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading application");
        }
    }

    /**
     * Static method to navigate back to login from anywhere in the app.
     */
    public static void showLoginScreen(Stage currentStage) {
        try {
            SessionManager.logout();

            // Preserve current window dimensions
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            boolean isMaximized = currentStage.isMaximized();

            FXMLLoader loader = new FXMLLoader(
                    LoginController.class.getResource("/com/hospital/view/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);
            currentStage.setScene(scene);
            currentStage.setTitle("Hospital Management System - Login");

            // Restore maximized state
            if (isMaximized) {
                currentStage.setMaximized(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
