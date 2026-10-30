package com.example.eskwela;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load User Data
        loadProfileData();

        // Setup Logout Button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Trigger the confirmation dialog instead of logging out immediately
            showLogoutConfirmationDialog();
        });

        // Setup Menu Row Click Listeners
        findViewById(R.id.rowEditProfile).setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, ProfileSetupActivity.class)));
        findViewById(R.id.rowReminders).setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class)));
        findViewById(R.id.rowSecurity).setOnClickListener(v -> showAccountSecurityDialog());
        findViewById(R.id.rowUpdate).setOnClickListener(v -> UpdateHelper.downloadAndInstallUpdate(this));

        // Setup Online Sync Toggle
        SwitchMaterial switchSync = findViewById(R.id.switchOnlineSync);
        if (switchSync != null) {
            SharedPreferences prefs = getSharedPreferences("eskwela_settings", MODE_PRIVATE);
            boolean isSyncEnabled = prefs.getBoolean("KEY_ONLINE_SYNC", true);
            switchSync.setChecked(isSyncEnabled);

            switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("KEY_ONLINE_SYNC", isChecked).apply();
                if (isChecked) {
                    db.enableNetwork()
                            .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Online Auto-Sync ENABLED. Cloud sync active.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error enabling sync: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    db.disableNetwork()
                            .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Online Auto-Sync DISABLED. Local mode active.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error disabling sync: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        }

        // Setup Dark Mode Toggle
        SwitchMaterial switchDark = findViewById(R.id.switchDarkMode);
        if (switchDark != null) {
            SharedPreferences prefs = getSharedPreferences("eskwela_settings", MODE_PRIVATE);
            boolean isDark = prefs.getBoolean("KEY_DARK_MODE", false);
            switchDark.setChecked(isDark);

            switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked != prefs.getBoolean("KEY_DARK_MODE", false)) {
                    prefs.edit().putBoolean("KEY_DARK_MODE", isChecked).apply();
                    if (isChecked) {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    recreate();
                }
            });
        }

        // Setup Admin Center Click Listener
        View cardAdmin = findViewById(R.id.cardAdminCenter);
        if (cardAdmin != null) {
            cardAdmin.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class)));
        }
        View btnAdmin = findViewById(R.id.btnAdminControlDashboard);
        if (btnAdmin != null) {
            btnAdmin.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class)));
        }


        // Setup Bottom Navigation
        setupBottomNavigation();
    }



    private void showAccountSecurityDialog() {
        String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "N/A";
        new AlertDialog.Builder(this)
                .setTitle("Account & Security")
                .setMessage("Email: " + email + "\nSecurity Status: Protected\n\nWould you like to send a password reset email?")
                .setPositiveButton("Reset Password", (dialog, which) -> {
                    if (email != null && !email.isEmpty() && !email.equals("N/A")) {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out of ESKWELA?")
                .setPositiveButton("Yes, Logout", (dialog, which) -> {
                    // User confirmed, proceed with logout
                    performLogout();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User cancelled, dismiss the dialog
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        // Clear back stack so user can't press back to return to the app
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_schedule) {
                startActivity(new Intent(ProfileActivity.this, MyScheduleActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_tasks) {
                startActivity(new Intent(ProfileActivity.this, TasksActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_study) {
                startActivity(new Intent(ProfileActivity.this, StudyActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void loadProfileData() {
        TextView tvInitials = findViewById(R.id.tvProfileInitials);
        TextView tvFullName = findViewById(R.id.tvUserFullName);
        TextView tvCourseYear = findViewById(R.id.tvUserCourseYear);
        TextView tvSchool = findViewById(R.id.tvUserSchool);
        TextView tvStudentId = findViewById(R.id.tvUserStudentId);
        TextView tvStorageDetails = findViewById(R.id.tvStorageDetails);

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            String email = mAuth.getCurrentUser().getEmail();

            // Check admin email immediately
            if (email != null && (email.toLowerCase().contains("admin") || email.equals("admin@eskwela.com"))) {
                View cardAdmin = findViewById(R.id.cardAdminCenter);
                if (cardAdmin != null) {
                    cardAdmin.setVisibility(View.VISIBLE);
                }
            }

            // 1. Fetch User Profile Data
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String course = documentSnapshot.getString("course");
                            String year = documentSnapshot.getString("year");
                            String school = documentSnapshot.getString("school");
                            String studentId = documentSnapshot.getString("studentId");
                            String role = documentSnapshot.getString("role");

                            if (fullName != null && !fullName.isEmpty()) {
                                tvFullName.setText(fullName);
                                String[] parts = fullName.split("\\s+");
                                String initials = "";
                                if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].substring(0, 1).toUpperCase();
                                if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) initials += parts[parts.length - 1].substring(0, 1).toUpperCase();
                                tvInitials.setText(initials);
                            }

                            String courseYear = "";
                            if (course != null && !course.isEmpty()) courseYear += course;
                            if (year != null && !year.isEmpty()) {
                                if (!courseYear.isEmpty()) courseYear += " · ";
                                courseYear += year;
                            }
                            if (!courseYear.isEmpty()) tvCourseYear.setText(courseYear);

                            if (school != null && !school.isEmpty()) {
                                tvSchool.setText(school);
                            }

                            if (studentId != null && !studentId.isEmpty()) {
                                tvStudentId.setText("ID: " + studentId);
                            }

                            if ("admin".equals(role) || (email != null && (email.toLowerCase().contains("admin") || email.equals("admin@eskwela.com")))) {
                                View cardAdmin = findViewById(R.id.cardAdminCenter);
                                if (cardAdmin != null) {
                                    cardAdmin.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });

            // 2. Fetch Dynamic Storage Info
            db.collection("schedules").whereEqualTo("userId", userId).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int classCount = queryDocumentSnapshots.size();
                        tvStorageDetails.setText(classCount + " classes · 0 tasks · 0 saved quizzes");
                    });
        }
    }
}