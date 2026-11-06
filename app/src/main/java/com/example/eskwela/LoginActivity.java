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

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> sendPasswordReset());
        }

        btnSubmitLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (isFinishing()) return;
                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser() == null) {
                                setLoading(false);
                                Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String userId = mAuth.getCurrentUser().getUid();
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("users").document(userId).get()
                                    .addOnSuccessListener(doc -> {
                                        if (isFinishing()) return;
                                        setLoading(false);
                                        if (AccountGuard.blockIfRestricted(LoginActivity.this, mAuth, doc)) {
                                            return;
                                        }
                                        String role = doc.getString("role");
                                        goToDashboard(email, role);
                                    })
                                    .addOnFailureListener(e -> {
                                        if (isFinishing()) return;
                                        setLoading(false);
                                        goToDashboard(email, null);
                                    });
                        } else {
                            setLoading(false);
                            Exception exception = task.getException();
                            String message = exception != null && exception.getMessage() != null
                                    ? exception.getMessage()
                                    : "Please check your credentials and try again.";
                            Toast.makeText(LoginActivity.this, "Login Failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void sendPasswordReset() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Could not send reset email: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void goToDashboard(String email, String role) {
        Toast.makeText(LoginActivity.this, "Login Successful ✨", Toast.LENGTH_SHORT).show();
        Class<?> target = AccountGuard.isAdmin(email, role) ? AdminDashboardActivity.class : DashboardActivity.class;
        Intent intent = new Intent(LoginActivity.this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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