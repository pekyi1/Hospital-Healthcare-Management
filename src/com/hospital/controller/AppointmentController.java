package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.service.HospitalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentController {

    @FXML
    private TableView<Appointment> appointmentTable;
    @FXML
    private TableColumn<Appointment, Integer> colId;
    @FXML
    private TableColumn<Appointment, Integer> colPatientId;
    @FXML
    private TableColumn<Appointment, Integer> colDoctorId;
    @FXML
    private TableColumn<Appointment, String> colDate;
    @FXML
    private TableColumn<Appointment, String> colStatus;
    @FXML
    private TableColumn<Appointment, String> colNotes;

    @FXML
    private TextField patientIdField;
    @FXML
    private TextField doctorIdField;
    @FXML
    private TextField dateField;
    @FXML
    private TextField notesField;
    @FXML
    private ComboBox<String> statusComboBox;

    private HospitalService hospitalService;
    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

    public void initialize() {
        hospitalService = new HospitalService();
        setupTableColumns();

        statusComboBox.setItems(FXCollections.observableArrayList("Scheduled", "Completed", "Cancelled"));
        statusComboBox.getSelectionModel().selectFirst();

        loadAppointments();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatientId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colDoctorId.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }

    private void loadAppointments() {
        try {
            List<Appointment> appointments = hospitalService.getAllAppointments();
            appointmentList.setAll(appointments);
            appointmentTable.setItems(appointmentList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appointments: " + e.getMessage());
        }
    }

    @FXML
    private void handleScheduleAppointment() {
        if (!validateInput())
            return;

        Appointment appointment = new Appointment();
        try {
            appointment.setPatientId(Integer.parseInt(patientIdField.getText()));
            appointment.setDoctorId(Integer.parseInt(doctorIdField.getText()));
        } catch (NumberFormatException e) {
            showAlert("Error", "Patient ID and Doctor ID must be numbers.");
            return;
        }

        try {
            LocalDateTime date = LocalDateTime.parse(dateField.getText()); // ISO-8601 default or custom? Using default
                                                                           // T separator
            appointment.setAppointmentDate(date);
        } catch (Exception e) {
            showAlert("Error", "Invalid Date Format. Use YYYY-MM-DDTHH:MM");
            return;
        }

        appointment.setStatus(statusComboBox.getValue());
        appointment.setNotes(notesField.getText());

        try {
            hospitalService.scheduleAppointment(appointment);
            loadAppointments();
            clearFields();
            showAlert("Success", "Appointment scheduled successfully.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to schedule appointment: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadAppointments();
    }

    private boolean validateInput() {
        if (patientIdField.getText().isEmpty() || doctorIdField.getText().isEmpty() ||
                dateField.getText().isEmpty()) {
            showAlert("Error", "Patient ID, Doctor ID, and Date are required.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        patientIdField.clear();
        doctorIdField.clear();
        dateField.clear();
        notesField.clear();
        statusComboBox.getSelectionModel().selectFirst();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
