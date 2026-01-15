package com.hospital.util;

import com.hospital.model.User;

/**
 * Singleton session manager to track the currently logged-in user.
 */
public class SessionManager {

    private static User currentUser = null;

    private SessionManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Sets the current logged-in user.
     * 
     * @param user The authenticated user
     */
    public static void login(User user) {
        currentUser = user;
    }

    /**
     * Clears the current session (logout).
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Gets the currently logged-in user.
     * 
     * @return The current user, or null if not logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks if the current user has the specified role.
     * 
     * @param role The role to check
     * @return true if the current user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        return currentUser != null && role.equals(currentUser.getRole());
    }

    /**
     * Gets the reference ID for the current user (doctor_id or patient_id).
     * 
     * @return The reference ID, or null if not applicable
     */
    public static Integer getReferenceId() {
        return currentUser != null ? currentUser.getReferenceId() : null;
    }

    /**
     * Checks if the current user is an admin.
     * 
     * @return true if admin, false otherwise
     */
    public static boolean isAdmin() {
        return hasRole("Admin");
    }

    /**
     * Checks if the current user is a doctor.
     * 
     * @return true if doctor, false otherwise
     */
    public static boolean isDoctor() {
        return hasRole("Doctor");
    }

    /**
     * Checks if the current user is a patient.
     * 
     * @return true if patient, false otherwise
     */
    public static boolean isPatient() {
        return hasRole("Patient");
    }
}
