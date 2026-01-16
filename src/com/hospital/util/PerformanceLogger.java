package com.hospital.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class PerformanceLogger {

    private static final String REPORT_FILE = "performance_report.csv";

    public static void log(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        String message = String.format("[Performance] %s completed in %d ms", operation, duration);
        System.out.println(message);

        // Append to report file
        try {
            Path path = Paths.get(REPORT_FILE);
            String timestamp = LocalDateTime.now().toString();
            String csvLine = String.format("%s,%s,%d%n", timestamp, operation, duration);

            if (!Files.exists(path)) {
                Files.writeString(path, "Timestamp,Operation,Duration_ms\n");
            }
            Files.writeString(path, csvLine, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write to performance report: " + e.getMessage());
        }
    }
}
