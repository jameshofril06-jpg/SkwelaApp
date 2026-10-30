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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnSubmitLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // Bind Views
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        // Trigger Entrance Animations
        setupAnimations();

        btnSubmitLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Turn on Loading State
            setLoading(true);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("users").document(userId).get()
                                    .addOnSuccessListener(doc -> {
                                        setLoading(false);
                                        if (doc.exists()) {
                                            String status = doc.getString("status");
                                            if ("suspended".equalsIgnoreCase(status) || "deactivated".equalsIgnoreCase(status) || "archived".equalsIgnoreCase(status)) {
                                                mAuth.signOut();
                                                new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this)
                                                        .setTitle("Account " + status.toUpperCase())
                                                        .setMessage("Your ESKWELA account is currently " + status + ". Please contact the system administrator.")
                                                        .setPositiveButton("OK", null)
                                                        .show();
                                                return;
                                            }
                                        }
                                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        setLoading(false);
                                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        } else {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Changes the Login button UI to show a loading state.
     */
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            btnSubmitLogin.setText("Logging in...");
            btnSubmitLogin.setBackgroundResource(R.drawable.bg_button_orange_disabled);
            btnSubmitLogin.setEnabled(false); // Prevent multiple clicks
            etEmail.setEnabled(false);        // Optional: Disable inputs while loading
            etPassword.setEnabled(false);
        } else {
            btnSubmitLogin.setText("Login");
            btnSubmitLogin.setBackgroundResource(R.drawable.bg_button_orange);
            btnSubmitLogin.setEnabled(true);
            etEmail.setEnabled(true);
            etPassword.setEnabled(true);
        }
    }

    private void setupAnimations() {
        View[] viewsToAnimate = {
                findViewById(R.id.tvLoginTitle),
                findViewById(R.id.tvLoginSubtitle),
                findViewById(R.id.lblLoginEmail),
                findViewById(R.id.etLoginEmail),
                findViewById(R.id.lblLoginPassword),
                findViewById(R.id.etLoginPassword),
                findViewById(R.id.tvForgotPassword),
                findViewById(R.id.btnSubmitLogin),
                findViewById(R.id.tvGoToRegister)
        };

        long delay = 100;
        for (View view : viewsToAnimate) {
            if (view != null) {
                view.setAlpha(0f);
                view.setTranslationY(50f);
                view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setStartDelay(delay)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                delay += 80;
            }
        }
    }
}