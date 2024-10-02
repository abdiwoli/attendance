package com.example.attendance;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    EditText emailSignup, name, password, confirmPassword;
    TextView loginLink;
    Button nextButton, createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_activity);

        // Step 1 fields
        name = findViewById(R.id.editTextName);
        emailSignup = findViewById(R.id.editTextEmail);
        nextButton = findViewById(R.id.nextButton);

        // Step 2 fields (hidden initially)
        password = findViewById(R.id.editTextPassword);
        confirmPassword = findViewById(R.id.editTextConfirmPassword);
        createAccountButton = findViewById(R.id.createAccountButton);

        loginLink = findViewById(R.id.loginLink);
        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();
        });

        nextButton.setOnClickListener(view -> {
            String nameText = name.getText().toString();
            String emailText = emailSignup.getText().toString();
            String validationMessage = validateEmail(emailText);

            if (validationMessage.equals("signed up")) {
                // Hide Step 1 and show Step 2
                nextButton.setVisibility(View.GONE);
                password.setVisibility(View.VISIBLE);
                confirmPassword.setVisibility(View.VISIBLE);
                createAccountButton.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, validationMessage, Toast.LENGTH_SHORT).show();
            }
        });

        createAccountButton.setOnClickListener(view -> {
            String passwordText = password.getText().toString();
            String confirmPasswordText = confirmPassword.getText().toString();

            if (passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!passwordText.equals(confirmPasswordText)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                DBHandler dbHandler = new DBHandler(this);
                Long result = dbHandler.addUser(name.getText().toString(), emailSignup.getText().toString(), passwordText);
                if (result == -1) {
                    Toast.makeText(this, "Error occurred while adding user", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    String validateEmail(String email) {
        if (email.isEmpty()) {
            return "Please fill in the email field";
        } else {
            DBHandler dbHandler = new DBHandler(this);
            Cursor user = dbHandler.getUserByEmail(email);
            if (user != null && user.getCount() > 0) {
                return "User already exists";
            }
        }
        return "signed up";
    }
}
