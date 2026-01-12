package com.hospital.controller;

import com.hospital.service.HospitalService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

public class DashboardController {

    @FXML
    private Label visitorCountLabel;
    @FXML
    private Label patientCountLabel;
    @FXML
    private Label doctorCountLabel;

    @FXML
    private BarChart<String, Number> patientChart;
    @FXML
    private PieChart departmentChart;

    private HospitalService hospitalService;

    public void initialize() {
        hospitalService = new HospitalService();
        loadStatistics();
        setupCharts();
    }

    private void setupCharts() {
        // Patient Statistics (Real Data from DB via Service)
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Appointments Last 7 Days");

        try {
            java.util.Map<String, Integer> stats = hospitalService.getAppointmentsPerDay();
            // Order: Mon -> Sun (Simple approach, or ordered by keys if Map is not sorted)
            // Ideally use a LinkedHashMap in service or sort here.
            // For now, explicit add based on day names ensure order.
            String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
            for (String day : days) {
                series.getData().add(new XYChart.Data<>(day, stats.getOrDefault(day, 0)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        patientChart.getData().clear();
        patientChart.getData().add(series);

        // Department Distribution (Real Data)
        try {
            java.util.Map<String, Integer> deptStats = hospitalService.getDoctorSpecializationStats();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (java.util.Map.Entry<String, Integer> entry : deptStats.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            departmentChart.setData(pieData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                int pCount = hospitalService.getPatientCount();
                int dCount = hospitalService.getDoctorCount();
                int appCount = hospitalService.getAppointmentCount(); // Use total appointments as "Visitors/Activity"

                Platform.runLater(() -> {
                    if (visitorCountLabel != null)
                        visitorCountLabel.setText(String.valueOf(appCount));
                    if (patientCountLabel != null)
                        patientCountLabel.setText(String.valueOf(pCount));
                    if (doctorCountLabel != null)
                        doctorCountLabel.setText(String.valueOf(dCount));
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

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
