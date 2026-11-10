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
    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnCreateAccount;

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
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        // Trigger Animations
        setupAnimations();
        setupPasswordToggle(etPassword);
        setupPasswordToggle(etConfirmPassword);

        btnCreateAccount.setOnClickListener(v -> {
            String fullName = etFullName != null ? etFullName.getText().toString().trim() : "";
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreateAccount.setEnabled(false);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (isFinishing()) return;
                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser() == null) {
                                btnCreateAccount.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, "Account created, but session is missing. Please log in.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String userId = mAuth.getCurrentUser().getUid();
                            String uniqueSuffix = userId.length() >= 4 ? userId.substring(0, 4).toUpperCase() : userId.toUpperCase();
                            String generatedId = "STU-" + uniqueSuffix;

                            Map<String, Object> initialData = new HashMap<>();
                            initialData.put("studentId", generatedId);
                            initialData.put("email", email);
                            initialData.put("fullName", fullName);
                            initialData.put("status", "active");

                            db.collection("users").document(userId).set(initialData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
                                        intent.putExtra("fullName", fullName);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnCreateAccount.setEnabled(true);
                                        Toast.makeText(RegisterActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            btnCreateAccount.setEnabled(true);
                            Exception exception = task.getException();
                            String message = exception != null && exception.getMessage() != null
                                    ? exception.getMessage()
                                    : "Could not create account.";
                            Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupPasswordToggle(EditText editText) {
        if (editText == null) return;
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
        final boolean[] isVisible = {false};

        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (editText.getCompoundDrawables()[DRAWABLE_RIGHT] != null &&
                        event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 48)) {
                    isVisible[0] = !isVisible[0];
                    v.performClick();
                    if (isVisible[0]) {
                        editText.setTransformationMethod(android.text.method.HideReturnsTransformationMethod.getInstance());
                    } else {
                        editText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                    }
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
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