package com.hospital.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EnvUtil {
    private static final Map<String, String> ENV = new HashMap<>();

    static {
        loadEnv();
    }

    private static void loadEnv() {
        // Try looking in the project root
        Path path = Paths.get(".env");
        if (!Files.exists(path)) {
            // Fallback for different execution contexts (e.g. running from bin)
            path = Paths.get("../.env");
        }

        if (Files.exists(path)) {
            try (Stream<String> lines = Files.lines(path)) {
                lines.filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                        .forEach(line -> {
                            String[] parts = line.split("=", 2);
                            if (parts.length == 2) {
                                ENV.put(parts[0].trim(), parts[1].trim());
                            }
                        });
                System.out.println("✅ Loaded environment variables from .env");
            } catch (IOException e) {
                System.err.println(
                        "⚠️ Should not happen: Failed to read .env file despite it existing: " + e.getMessage());
            }
        } else {
            System.err.println("⚠️ No .env file found. Ensure it exists in the project root.");
        }
    }

    public static String get(String key) {
        return ENV.get(key);
    }

    public static String get(String key, String defaultValue) {
        return ENV.getOrDefault(key, defaultValue);
    }
}
