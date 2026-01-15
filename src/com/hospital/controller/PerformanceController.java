package com.hospital.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PerformanceController {

    @FXML
    private TableView<PerformanceEntry> performanceTable;
    @FXML
    private TableColumn<PerformanceEntry, String> timestampColumn;
    @FXML
    private TableColumn<PerformanceEntry, String> operationColumn;
    @FXML
    private TableColumn<PerformanceEntry, Integer> durationColumn;
    @FXML
    private Label totalOperationsLabel;
    @FXML
    private Label avgDurationLabel;
    @FXML
    private Label fastestLabel;
    @FXML
    private Label slowestLabel;
    @FXML
    private Button refreshButton;

    private ObservableList<PerformanceEntry> entries = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up table columns
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        operationColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        // Style duration column based on performance
        durationColumn.setCellFactory(column -> new TableCell<PerformanceEntry, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + " ms");
                    // Color code based on duration
                    if (item < 100) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green - Fast
                    } else if (item < 500) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Orange - Medium
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red - Slow
                    }
                }
            }
        });

        performanceTable.setItems(entries);
        loadPerformanceData();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing performance data...");
        loadPerformanceData();
        performanceTable.refresh();
        System.out.println("Loaded " + entries.size() + " entries.");
    }

    private void loadPerformanceData() {
        entries.clear();

        Path csvPath = Paths.get("performance_report.csv");

        if (!Files.exists(csvPath)) {
            totalOperationsLabel.setText("No data file found");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath.toFile()))) {
            String line;
            boolean isFirstLine = true;
            List<PerformanceEntry> allEntries = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String timestamp = parts[0].trim();
                    String operation = parts[1].trim();
                    int duration = 0;
                    try {
                        duration = Integer.parseInt(parts[2].trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    allEntries.add(new PerformanceEntry(timestamp, operation, duration));
                }
            }

            // Sort by timestamp descending (newest first)
            Collections.reverse(allEntries);
            entries.addAll(allEntries);

            // Calculate statistics
            updateStatistics(allEntries);

        } catch (Exception e) {
            totalOperationsLabel.setText("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatistics(List<PerformanceEntry> allEntries) {
        if (allEntries.isEmpty()) {
            totalOperationsLabel.setText("0");
            avgDurationLabel.setText("N/A");
            fastestLabel.setText("N/A");
            slowestLabel.setText("N/A");
            return;
        }

        int total = allEntries.size();
        double avg = allEntries.stream().mapToInt(PerformanceEntry::getDuration).average().orElse(0);
        int min = allEntries.stream().mapToInt(PerformanceEntry::getDuration).min().orElse(0);
        int max = allEntries.stream().mapToInt(PerformanceEntry::getDuration).max().orElse(0);

        // Find fastest and slowest operations
        PerformanceEntry fastest = allEntries.stream()
                .min(Comparator.comparingInt(PerformanceEntry::getDuration)).orElse(null);
        PerformanceEntry slowest = allEntries.stream()
                .max(Comparator.comparingInt(PerformanceEntry::getDuration)).orElse(null);

        totalOperationsLabel.setText(String.valueOf(total));
        avgDurationLabel.setText(String.format("%.1f ms", avg));
        fastestLabel.setText(fastest != null ? fastest.getOperation() + " (" + min + " ms)" : "N/A");
        slowestLabel.setText(slowest != null ? slowest.getOperation() + " (" + max + " ms)" : "N/A");
    }

    // Inner class for table data
    public static class PerformanceEntry {
        private final SimpleStringProperty timestamp;
        private final SimpleStringProperty operation;
        private final SimpleIntegerProperty duration;

        public PerformanceEntry(String timestamp, String operation, int duration) {
            this.timestamp = new SimpleStringProperty(timestamp);
            this.operation = new SimpleStringProperty(operation);
            this.duration = new SimpleIntegerProperty(duration);
        }

        public String getTimestamp() {
            return timestamp.get();
        }

        public String getOperation() {
            return operation.get();
        }

        public int getDuration() {
            return duration.get();
        }
    }
}
