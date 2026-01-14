package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.HospitalService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AppointmentFormDialogController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField patientFirstNameField;
    @FXML
    private TextField patientLastNameField;
    @FXML
    private TextField doctorFirstNameField;
    @FXML
    private TextField doctorLastNameField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeField;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TextArea notesField;

    private HospitalService hospitalService;
    private Appointment appointment;
    private Stage dialogStage;
    private boolean saveSuccessful = false;

    // Store found patient/doctor for editing
    private Patient foundPatient;
    private Doctor foundDoctor;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void initialize() {
        hospitalService = new HospitalService();
        statusComboBox.setItems(FXCollections.observableArrayList("Scheduled", "Completed", "Cancelled"));
        statusComboBox.getSelectionModel().selectFirst();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
        if (appointment != null && appointment.getId() > 0) {
            titleLabel.setText("Edit Appointment");

            // Load patient name from ID
            try {
                Patient patient = hospitalService.getPatientById(appointment.getPatientId());
                if (patient != null) {
                    patientFirstNameField.setText(patient.getFirstName());
                    patientLastNameField.setText(patient.getLastName());
                }
            } catch (SQLException e) {
                // Leave fields empty if can't load
            }

            // Load doctor name from ID
            try {
                Doctor doctor = hospitalService.getDoctorById(appointment.getDoctorId());
                if (doctor != null) {
                    doctorFirstNameField.setText(doctor.getFirstName());
                    doctorLastNameField.setText(doctor.getLastName());
                }
            } catch (SQLException e) {
                // Leave fields empty if can't load
            }

            if (appointment.getAppointmentDate() != null) {
                datePicker.setValue(appointment.getAppointmentDate().toLocalDate());
                timeField.setText(appointment.getAppointmentDate().toLocalTime().format(TIME_FORMATTER));
            }

            statusComboBox.setValue(appointment.getStatus());
            notesField.setText(appointment.getNotes());
        } else {
            titleLabel.setText("Add Appointment");
            this.appointment = new Appointment();
            datePicker.setValue(LocalDate.now());
            timeField.setText("10:00");
        }
    }

    @FXML
    private void handleSave() {
        // First validate and lookup patient
        String patientFirstName = patientFirstNameField.getText().trim();
        String patientLastName = patientLastNameField.getText().trim();

        if (patientFirstName.isEmpty() || patientLastName.isEmpty()) {
            showAlert("Validation Error", "Please enter both the patient's first and last name.");
            return;
        }

        try {
            foundPatient = hospitalService.getPatientByName(patientFirstName, patientLastName);
            if (foundPatient == null) {
                showAlert("Patient Not Found",
                        "No patient found with the name: " + patientFirstName + " " + patientLastName +
                                "\n\nPlease check the name and try again.");
                patientFirstNameField.requestFocus();
                return;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to lookup patient: " + e.getMessage());
            return;
        }

        // Validate and lookup doctor
        String doctorFirstName = doctorFirstNameField.getText().trim();
        String doctorLastName = doctorLastNameField.getText().trim();

        if (doctorFirstName.isEmpty() || doctorLastName.isEmpty()) {
            showAlert("Validation Error", "Please enter both the doctor's first and last name.");
            return;
        }

        try {
            foundDoctor = hospitalService.getDoctorByName(doctorFirstName, doctorLastName);
            if (foundDoctor == null) {
                showAlert("Doctor Not Found",
                        "No doctor found with the name: " + doctorFirstName + " " + doctorLastName +
                                "\n\nPlease check the name and try again.");
                doctorFirstNameField.requestFocus();
                return;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to lookup doctor: " + e.getMessage());
            return;
        }

        // Validate date and time
        if (datePicker.getValue() == null) {
            showAlert("Validation Error", "Please select a date.");
            return;
        }

        String time = timeField.getText().trim();
        if (time.isEmpty()) {
            showAlert("Validation Error", "Please enter a time (HH:MM format).");
            return;
        }

        LocalTime parsedTime;
        try {
            parsedTime = LocalTime.parse(time, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            showAlert("Validation Error", "Invalid time format. Please use HH:MM (e.g., 10:00)");
            return;
        }

        // Set appointment values
        appointment.setPatientId(foundPatient.getId());
        appointment.setDoctorId(foundDoctor.getId());
        appointment.setAppointmentDate(LocalDateTime.of(datePicker.getValue(), parsedTime));
        appointment.setStatus(statusComboBox.getValue());
        appointment.setNotes(notesField.getText().trim());

        try {
            if (appointment.getId() > 0) {
                hospitalService.updateAppointment(appointment);
            } else {
                hospitalService.scheduleAppointment(appointment);
            }
            saveSuccessful = true;
            dialogStage.close();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save appointment: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
