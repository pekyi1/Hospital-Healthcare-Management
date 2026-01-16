package com.hospital.controller;

import com.hospital.service.PatientService;
import com.hospital.service.DoctorService;
import com.hospital.service.AppointmentService;
import com.hospital.service.DepartmentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

public class DashboardController {

    @FXML
    private Label lblDashboardWelcome;
    @FXML
    private Label patientCountLabel;
    @FXML
    private Label doctorCountLabel;
    @FXML
    private Label appointmentCountLabel;
    @FXML
    private Label departmentCountLabel;

    @FXML
    private BarChart<String, Number> patientChart;
    @FXML
    private PieChart departmentChart;

    private PatientService patientService;
    private DoctorService doctorService;
    private AppointmentService appointmentService;
    private DepartmentService departmentService;

    public void initialize() {
        patientService = new PatientService();
        doctorService = new DoctorService();
        appointmentService = new AppointmentService();
        departmentService = new DepartmentService();

        // Update welcome message based on current role
        MainController mainController = MainController.getInstance();
        if (mainController != null && lblDashboardWelcome != null) {
            String role = mainController.getCurrentRole();
            lblDashboardWelcome.setText("Welcome back, " + role + " \uD83D\uDC4B");
        }

        loadStatistics();
        setupCharts();
    }

    private void setupCharts() {
        // Enable animations on charts - bars will grow from 0, pie slices will expand
        patientChart.setAnimated(true);
        departmentChart.setAnimated(true);

        // Add an entry animation (Scale Up) for the charts themselves
        animateChartEntry(patientChart);
        animateChartEntry(departmentChart);

        // Patient Statistics - Shows patients registered per day of week
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patients by Registration Day");

        try {
            java.util.Map<String, Integer> stats = patientService.getPatientsPerDayOfWeek();
            String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
            for (String day : days) {
                series.getData().add(new XYChart.Data<>(day, stats.getOrDefault(day, 0)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Defer data addition with a slight delay to ensure the scene is ready and
        // animations trigger visibly
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
        pause.setOnFinished(e -> {
            patientChart.getData().clear();
            patientChart.getData().add(series);

            // Fix for "3.5 patients" issue:
            // Disable auto-ranging and manually set upper bound to an integer
            NumberAxis yAxis = (NumberAxis) patientChart.getYAxis();
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setTickUnit(1);
            yAxis.setMinorTickVisible(false);

            int maxVal = 0;
            try {
                // Find max value in current data
                Map<String, Integer> currentStats = patientService.getPatientsPerDayOfWeek();
                for (int count : currentStats.values()) {
                    if (count > maxVal)
                        maxVal = count;
                }
            } catch (SQLException ex) {
                /* ignore, safety fallback */
            }

            // Ensure upper bound is at least 5 for looks, or max + 1
            int upperBound = (maxVal < 5) ? 5 : (maxVal + 2);
            yAxis.setUpperBound(upperBound);

            // Force integer labels
            yAxis.setTickLabelFormatter(new StringConverter<Number>() {
                @Override
                public String toString(Number object) {
                    return String.valueOf(object.intValue());
                }

                @Override
                public Number fromString(String string) {
                    return Integer.parseInt(string);
                }
            });

            // Department Distribution
            try {
                java.util.Map<String, Integer> deptStats = doctorService.getDoctorsPerDepartment();
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                for (java.util.Map.Entry<String, Integer> entry : deptStats.entrySet()) {
                    pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
                departmentChart.setData(pieData);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        pause.play();
    }

    private void animateChartEntry(javafx.scene.Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(800), node);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();

        FadeTransition ft = new FadeTransition(Duration.millis(800), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                int pCount = patientService.getPatientCount();
                int dCount = doctorService.getDoctorCount();
                int appCount = appointmentService.getAppointmentCount();
                int deptCount = departmentService.getAllDepartments().size();

                Platform.runLater(() -> {
                    if (patientCountLabel != null)
                        patientCountLabel.setText(String.valueOf(pCount));
                    if (doctorCountLabel != null)
                        doctorCountLabel.setText(String.valueOf(dCount));
                    if (appointmentCountLabel != null)
                        appointmentCountLabel.setText(String.valueOf(appCount));
                    if (departmentCountLabel != null)
                        departmentCountLabel.setText(String.valueOf(deptCount));
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
