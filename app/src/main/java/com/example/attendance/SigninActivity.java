package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SigninActivity extends AppCompatActivity {
    TextView loginLink;
    EditText email, password;
    Button nextButton;
    String user_email, user_password, user_name, user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity);

        nextButton = findViewById(R.id.nextButtonlpgin);
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);

        loginLink = findViewById(R.id.loginLink);
        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        nextButton.setOnClickListener(view -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();
            DBHandler dbHandler = new DBHandler(this);
            Cursor user = dbHandler.getUserByEmail(emailText);

            // Combine the null and count checks
            if (user == null || user.getCount() == 0) {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            } else if (user.moveToFirst()) {
                // Retrieve user details
                user_name = user.getString(1);
                user_email = user.getString(2);
                user_password = user.getString(3);
                user_id = user.getString(0);


                user.close(); // Close the cursor after use

                // Check if entered password matches the stored password
                if (passwordText.equals(user_password)) {
                    // Store user data in shared preferences

                    // Proceed to the main activity
                    Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                    intent.putExtra("email", emailText);
                    intent.putExtra("name", user_name);
                    intent.putExtra("password", passwordText);
                    intent.putExtra("id", user_id);
                    intent.putExtra("logedIn", true);
                    startActivity(intent);
                    finish();
                } else {
                    // Passwords don't match
                    Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
}