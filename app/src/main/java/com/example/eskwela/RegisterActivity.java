package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        // Trigger Animations
        setupAnimations();

        btnCreateAccount.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();

                            String uniqueSuffix = userId.substring(0, 4).toUpperCase();
                            String generatedId = "STU-" + uniqueSuffix;

                            Map<String, Object> initialData = new HashMap<>();
                            initialData.put("studentId", generatedId);
                            initialData.put("email", email);

                            db.collection("users").document(userId).set(initialData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Failed to save ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupAnimations() {
        // Array of views to animate (top to bottom)
        View[] viewsToAnimate = {
                findViewById(R.id.tvHeaderTitle),     // "Create your account"
                findViewById(R.id.tvSubtitle),        // "This will only take a minute."
                findViewById(R.id.lblFullName),       // "FULL NAME"
                findViewById(R.id.etFullName),        // Input field
                findViewById(R.id.lblEmail),          // "EMAIL ADDRESS"
                findViewById(R.id.etEmail),           // Input field
                findViewById(R.id.lblPassword),       // "PASSWORD"
                findViewById(R.id.etPassword),        // Input field
                findViewById(R.id.lblConfirmPassword),// "CONFIRM PASSWORD"
                findViewById(R.id.etConfirmPassword), // Input field
                findViewById(R.id.btnCreateAccount),  // Button
                findViewById(R.id.tvLoginLink)        // "Already have an account?"
        };

        // Staggered animation loop
        long delay = 100;
        for (View view : viewsToAnimate) {
            if (view != null) {
                view.setAlpha(0f);
                view.setTranslationY(50f); // Slide up from 50 pixels down

                view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setStartDelay(delay)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();

                delay += 60; // Slightly faster stagger because there are more elements
            }
        }
    }
}