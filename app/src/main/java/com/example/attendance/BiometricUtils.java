// BiometricUtils.java
package com.example.attendance;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BiometricUtils {

    public static String hashBiometricTemplate(String biometricData) throws NoSuchAlgorithmException {
        // Create a MessageDigest instance for SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        // Hash the biometric data
        byte[] hash = digest.digest(biometricData.getBytes());

        // Convert the byte array into a hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString(); // Return the hashed value
    }
}
