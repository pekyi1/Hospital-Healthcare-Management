package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.model.PatientFeedback;
import com.hospital.service.FeedbackService;
import com.hospital.service.PatientService;
import com.hospital.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FeedbackController {

    @FXML
    private TableView<PatientFeedback> feedbackTable;
    @FXML
    private TableColumn<PatientFeedback, Integer> idColumn;
    @FXML
    private TableColumn<PatientFeedback, String> patientNameColumn;
    @FXML
    private TableColumn<PatientFeedback, Integer> ratingColumn;
    @FXML
    private TableColumn<PatientFeedback, String> commentsColumn;
    @FXML
    private TableColumn<PatientFeedback, String> dateColumn;

    @FXML
    private VBox patientOnlyPane;

    private FeedbackService feedbackService;
    private PatientService patientService;

    public FeedbackController() {
    }

    @FXML
    public void initialize() {
        feedbackService = new FeedbackService();
        patientService = new PatientService();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("feedbackDate"));

        // Check role and configure UI
        boolean isPatient = SessionManager.isPatient();

        if (isPatient) {
            // Patients can only submit feedback, not view the table
            if (feedbackTable != null) {
                feedbackTable.setVisible(false);
                feedbackTable.setManaged(false);
            }

            if (patientOnlyPane != null) {
                patientOnlyPane.setVisible(true);
                patientOnlyPane.setManaged(true);
            }
        } else {
            // Admins and Doctors can see the table
            loadFeedback();
        }
    }

    private void loadFeedback() {
        try {
            List<PatientFeedback> feedbackList = feedbackService.getAllFeedback();
            feedbackTable.setItems(FXCollections.observableArrayList(feedbackList));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddFeedback() {
        Dialog<PatientFeedback> dialog = new Dialog<>();
        dialog.setTitle("Submit Feedback");
        dialog.setHeaderText("Enter Patient Feedback");

        ButtonType saveButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        Slider ratingSlider = new Slider(1, 5, 5);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);

        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Comments");
        commentsArea.setPrefRowCount(3);

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);

        grid.add(new Label("Rating (1-5):"), 0, 2);
        grid.add(ratingSlider, 1, 2);
        grid.add(new Label("Comments:"), 0, 3);
        grid.add(commentsArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String firstName = firstNameField.getText();
                    String lastName = lastNameField.getText();

                    if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
                        return null;
                    }

                    // Lookup patient by name using search
                    List<Patient> searchResults = patientService.searchPatients(firstName + " " + lastName);
                    Patient patient = null;
                    // Ideally we should find exact match, but for now take first if available
                    if (!searchResults.isEmpty()) {
                        // Simple heuristic: Try to match both names
                        for (Patient p : searchResults) {
                            if (p.getFirstName().equalsIgnoreCase(firstName)
                                    && p.getLastName().equalsIgnoreCase(lastName)) {
                                patient = p;
                                break;
                            }
                        }
                        // If no exact match found but search returned something, maybe strict check is
                        // needed?
                        // The searchPatients might be broad.
                        if (patient == null) {
                            // Fallback or just fail? For now, let's just pick first if "close enough" isn't
                            // implemented?
                            // Actually searchPatients does LIKE query. Let's assume user is accurate.
                            // Better yet, let's require exact match for safety in "Login-less" feedback
                            // context (simulation).
                            // If exact match not found in list, return null.
                        }
                    }

                    if (patient == null) {
                        return null;
                    }

                    int rating = (int) ratingSlider.getValue();
                    String comments = commentsArea.getText();
                    return new PatientFeedback(0, patient.getId(), rating, comments);

                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        // Add validation to prevent closing if patient not found
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Let the result converter handle logic, if null is returned, dialog closes
            // with empty result
            // which we handle in ifPresentOrElse below.
            // Wait, resultConverter returning null creates empty Optional result, dialog
            // closes.
        });

        Optional<PatientFeedback> result = dialog.showAndWait();
        result.ifPresentOrElse(fb -> {
            try {
                feedbackService.submitFeedback(fb);
                showAlert("Success", "Feedback submitted!");
                if (!SessionManager.isPatient()) {
                    loadFeedback(); // Refresh table only if view is visible
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to submit feedback: " + e.getMessage());
            }
        }, () -> {
            // Check if input was provided but patient not found
            String fn = firstNameField.getText();
            String ln = lastNameField.getText();
            if (fn != null && !fn.isEmpty() && ln != null && !ln.isEmpty()) {
                try {
                    // Quick re-check for error message
                    List<Patient> searchResults = patientService.searchPatients(fn + " " + ln);
                    boolean found = false;
                    for (Patient p : searchResults) {
                        if (p.getFirstName().equalsIgnoreCase(fn) && p.getLastName().equalsIgnoreCase(ln)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        showAlert("Error", "Patient not found: " + fn + " " + ln);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
