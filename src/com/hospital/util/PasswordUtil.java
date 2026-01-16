package com.hospital.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing using SHA-256.
 */
public class PasswordUtil {

    /**
     * Hashes a plain text password using SHA-256.
     * 
     * @param plainPassword The plain text password
     * @return The hexadecimal string representation of the hash
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a plain text password against a stored hash.
     * 
     * @param plainPassword  The plain text password to verify
     * @param hashedPassword The stored hash to compare against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String newHash = hashPassword(plainPassword);
        return newHash.equals(hashedPassword);
    }

    /**
     * Converts a byte array to a hexadecimal string.
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
