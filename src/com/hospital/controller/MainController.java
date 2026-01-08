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

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
