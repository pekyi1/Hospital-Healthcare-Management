package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.model.PatientNote;
import com.hospital.service.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientNoteDialogController {

    @FXML
    private Label patientNameLabel;
    @FXML
    private VBox notesContainer; // Container for list of notes
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private TextArea contentArea;
    @FXML
    private Button addButton;

    private PatientService patientService;
    private Patient currentPatient;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        patientService = new PatientService();
        categoryComboBox.getItems().addAll("General", "Nurse Log", "Vitals", "History", "Prescription");
        categoryComboBox.setValue("General");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        patientNameLabel.setText("Notes for: " + patient.getFirstName() + " " + patient.getLastName());
        loadNotes();
    }

    private void loadNotes() {
        notesContainer.getChildren().clear();

        List<PatientNote> notes = patientService.getPatientNotes(currentPatient.getId());

        if (notes.isEmpty()) {
            Label placeholder = new Label("No notes found for this patient.");
            placeholder.setStyle("-fx-text-fill: #95a5a6; -fx-padding: 10;");
            notesContainer.getChildren().add(placeholder);
        }

        for (PatientNote note : notes) {
            VBox card = new VBox(5);
            card.setStyle(
                    "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

            Label header = new Label(note.getCategory() + " • " + note.getCreatedAt().toString());
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 11px;");

            Label content = new Label();
            content.setWrapText(true);
            content.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

            // Build content string from Map
            StringBuilder sb = new StringBuilder();
            if (note.getContent() != null) {
                // If "text" key exists, show it primarily
                if (note.getContent().containsKey("text")) {
                    sb.append(note.getContent().get("text")).append("\n");
                }
                // Show other keys
                note.getContent().forEach((k, v) -> {
                    if (!k.equals("text")) {
                        sb.append("• ").append(k).append(": ").append(v).append("\n");
                    }
                });
            }
            content.setText(sb.toString().trim());

            card.getChildren().addAll(header, content);
            notesContainer.getChildren().add(card);

            // Add spacer
            notesContainer.getChildren().add(new javafx.scene.layout.Region() {
                {
                    setMinHeight(5);
                }
            });
        }
    }

    @FXML
    private void handleAddNote() {
        String category = categoryComboBox.getValue();
        String text = contentArea.getText().trim();

        if (text.isEmpty()) {
            showAlert("Error", "Note content cannot be empty.");
            return;
        }

        Map<String, String> content = new HashMap<>();
        content.put("text", text);

        // Example of "unstructured" logic: extract "key:value" lines?
        // For simplicity, we just store the main text.
        // Real implementation could parse lines like "BP: 120/80" into separate map
        // keys.

        PatientNote note = new PatientNote(currentPatient.getId(), category, content);

        try {
            patientService.addPatientNote(note);
            contentArea.clear();
            loadNotes(); // Refresh list
        } catch (Exception e) {
            showAlert("Error", "Failed to add note: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}
