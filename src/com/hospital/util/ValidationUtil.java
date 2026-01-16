package com.hospital.util;

import java.util.regex.Pattern;

/**
 * Utility class for common validation patterns used across the application.
 */
public class ValidationUtil {

    // Regex patterns
    public static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s'-]{2,50}$");

    // More robust email pattern handling standard formats
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Strict 10-digit phone number
    public static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    // Address pattern allowing alphanumeric, spaces, and common punctuation
    public static final Pattern ADDRESS_PATTERN = Pattern.compile("^[A-Za-z0-9\\s,.'-]{5,200}$");

    /**
     * Validates a name (First/Last).
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Validates an email address.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates a phone number.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates an address.
     */
    public static boolean isValidAddress(String address) {
        return address != null && ADDRESS_PATTERN.matcher(address).matches();
    }
}
