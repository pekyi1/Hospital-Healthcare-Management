package com.hospital.controller;

import com.hospital.service.HospitalService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardController {

    @FXML
    private Label patientCountLabel;
    @FXML
    private Label doctorCountLabel;
    @FXML
    private Label appointmentCountLabel;

    private HospitalService hospitalService;
    private MainController mainController;

    public void initialize() {
        hospitalService = new HospitalService();
        loadStatistics();
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                int pCount = hospitalService.getPatientCount();
                int dCount = hospitalService.getDoctorCount();
                int aCount = hospitalService.getAppointmentCount();

                Platform.runLater(() -> {
                    patientCountLabel.setText(String.valueOf(pCount));
                    doctorCountLabel.setText(String.valueOf(dCount));
                    appointmentCountLabel.setText(String.valueOf(aCount));
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Navigation Handlers - We need reference to MainController or access to the
    // scene structure
    // Since MainController handles the content switch, usually we'd need a callback
    // or shared model.
    // For simplicity, we will instantiate a new MainController or try to navigate
    // via looking up Scene.
    // BETTER APPROACH: Use Button actions to trigger menu events if possible, or
    // replicate loading.

    // Actually, MainController is the parent. We can't easily access it unless
    // passed.
    // Workaround: We will use the same logic as MainController to load views, but
    // into the parent StackPane?
    // This is tricky without a navigation service.
    // Let's implement basic navigation assuming we are inside the BorderPane ->
    // Center -> StackPane.

    @FXML
    private void goToPatients() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().loadView("/com/hospital/view/PatientView.fxml");
        }
    }

    @FXML
    private void goToAppointments() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().loadView("/com/hospital/view/AppointmentView.fxml");
        }
    }

    @FXML
    private void goToPrescriptions() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().loadView("/com/hospital/view/PrescriptionView.fxml");
        }
    }

}
