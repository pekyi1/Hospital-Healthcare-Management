package com.hospital.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static final String CONFIG_FILE = "src/config.properties";
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        // Load properties
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            URL = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/hospital_db");
            USER = props.getProperty("db.user", "postgres");
            PASSWORD = props.getProperty("db.password", "");
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Could not load config.properties. Using default credentials.");
            URL = "jdbc:postgresql://localhost:5432/hospital_db";
            USER = "postgres";
            PASSWORD = ""; // Fallback
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
