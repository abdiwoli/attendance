package com.example.attendance;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BiometricHandler {
    private final AppCompatActivity activity;

    public BiometricHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    public CompletableFuture<String> requestBiometricScan() {
        Executor executor = ContextCompat.getMainExecutor(activity);
        CompletableFuture<String> biometricResult = new CompletableFuture<>();

        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                biometricResult.completeExceptionally(new Exception(errString.toString()));
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                String biometricReference = "dummy_fingerprint_data"; // Example data
                biometricResult.complete(biometricReference);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                biometricResult.completeExceptionally(new Exception("Authentication failed"));
            }
        };

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, callback);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Scan your fingerprint to continue")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
        return biometricResult;
    }

    public interface BiometricCallback {
        void onBiometricAuthenticated(String hashedBiometricData); // Callback for authenticated user
        void onBiometricRegistered(String hashedBiometricData); // Callback for registered user
        void onError(Exception e); // Callback for error handling
    }

    public void registerBiometric(BiometricCallback callback) {
        requestBiometricScan().whenComplete((biometricReference, throwable) -> {
            if (throwable != null) {
                callback.onError(new Exception("An unexpected error occurred"));
            } else {
                try {
                    String hashedBiometricTemplate = BiometricUtils.hashBiometricTemplate(biometricReference);
                    callback.onBiometricRegistered(hashedBiometricTemplate); // Return hashed data
                } catch (NoSuchAlgorithmException e) {
                    callback.onError(e);
                }
            }
        });
    }

    public void loginBiometric(BiometricCallback callback) {
        requestBiometricScan().whenComplete((biometricReference, throwable) -> {
            if (throwable != null) {
                callback.onError(new Exception("Authentication failed"));
            } else {
                try {
                    String hashedBiometricData = BiometricUtils.hashBiometricTemplate(biometricReference);
                    callback.onBiometricAuthenticated(hashedBiometricData); // Return hashed data
                } catch (NoSuchAlgorithmException e) {
                    callback.onError(e);
                }
            }
        });
    }
}
