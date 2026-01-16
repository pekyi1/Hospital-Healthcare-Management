package com.hospital.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        // Load properties from .env
        URL = EnvUtil.get("DB_URL", "jdbc:postgresql://localhost:5432/hospital_db");
        USER = EnvUtil.get("DB_USER", "postgres");
        PASSWORD = EnvUtil.get("DB_PASSWORD", "");

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
