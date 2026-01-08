package com.hospital.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class PerformanceController {

    @FXML
    private TextArea logArea;

    @FXML
    public void initialize() {
        // In a real app, we'd bind this to a logging service.
        // For now, we display a static message explaining the feature.
        logArea.setText("Performance Monitoring Active.\n\n" +
                "Operations are currently being logged to the console/standard output.\n" +
                "To view real-time metrics, please check the console where the application was started.\n\n" +
                "Metrics tracked:\n" +
                "- Database Query Execution Time\n" +
                "- Cache Hit/Miss Rates (Implicit)\n" +
                "- Service Method Duration");
    }
}
