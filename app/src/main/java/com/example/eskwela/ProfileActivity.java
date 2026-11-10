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

        // Setup Logout Button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Trigger the confirmation dialog instead of logging out immediately
            showLogoutConfirmationDialog();
        });

        // Setup Menu Row Click Listeners
        findViewById(R.id.rowEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileSetupActivity.class);
            intent.putExtra("EDIT_MODE", true);
            startActivity(intent);
        });
        findViewById(R.id.rowReminders).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
            intent.putExtra("FROM_PROFILE", true);
            startActivity(intent);
        });
        findViewById(R.id.rowSecurity).setOnClickListener(v -> showAccountSecurityDialog());
        findViewById(R.id.rowUpdate).setOnClickListener(v -> UpdateHelper.downloadAndInstallUpdate(this));
        findViewById(R.id.rowAboutApp).setOnClickListener(v -> showAboutAppDialog());

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



    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void showAboutAppDialog() {
        String currentVersion = "1.1.7";
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {}

        String aboutMsg = "🎓 ESKWELA v" + currentVersion + "\n\n" +
                "Smart Student Productivity & Academic Management System designed for students and academic administrators.\n\n" +
                "🚀 What's New & Changelog (v" + currentVersion + "):\n" +
                "• 👁️ Show/Hide Password Toggle: Interactive eye icon on password inputs for secure viewing.\n" +
                "• 🖼️ User Profile Picture Picker: Select custom avatar photos from gallery stored directly in Firestore.\n" +
                "• 👤 Admin Profile Access & Navigation: Seamless access from Admin Control Center toolbar to Profile (Edit profile, Dark theme, OTA updates, About this app, Logout).\n" +
                "• 📢 Admin Home Announcements: Admin Home tab displays broadcast announcements management exclusively.\n" +
                "• 📅 Strict Date-Specific Schedule Filtering: Timetable views filter classes strictly by selected date.\n" +
                "• 🤖 Enrolled Study Load AI Quiz Generator & Ranking Leaderboard.\n" +
                "• ⏳ Admin User Suspension Time Tracking (e.g. 6h 42m remaining).\n" +
                "• ⏱️ Focus Session Presets (2m, 5m, 10m, 15m, 25m, 45m, 60m).\n" +
                "• 🌙 Global Dark Mode Text Contrast Fixes across all screens.\n" +
                "• ⚠️ Time Schedule Conflict Detection & Code-Validated Class Deletion.\n\n" +
                "© 2025 ESKWELA Team. All rights reserved.";

        new AlertDialog.Builder(this)
                .setTitle("About ESKWELA & Changelog (v" + currentVersion + ")")
                .setMessage(aboutMsg)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
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

        boolean isAdminUser = mAuth.getCurrentUser() != null && AccountGuard.isAdmin(mAuth.getCurrentUser().getEmail(), null);
        if (isAdminUser) {
            bottomNav.getMenu().clear();
            bottomNav.inflateMenu(R.menu.admin_bottom_nav_menu);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) return true;

            Class<?> targetClass = null;
            int targetIndex = 0;
            if (itemId == R.id.nav_home) {
                if (isAdminUser) {
                    startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class));
                    finish();
                    return true;
                } else {
                    targetClass = DashboardActivity.class;
                    targetIndex = 0;
                }
            } else if (itemId == R.id.nav_schedule) {
                targetClass = MyScheduleActivity.class;
                targetIndex = 1;
            } else if (itemId == R.id.nav_tasks) {
                targetClass = TasksActivity.class;
                targetIndex = 2;
            } else if (itemId == R.id.nav_study) {
                targetClass = StudyActivity.class;
                targetIndex = 3;
            }

            if (targetClass != null) {
                NavigationHelper.navigateWithTabAnimation(this, 4, targetIndex, targetClass);
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

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            String email = mAuth.getCurrentUser().getEmail();

            View cardAdmin = findViewById(R.id.cardAdminCenter);
            if (cardAdmin != null) {
                cardAdmin.setVisibility(View.GONE);
            }

            // 1. Fetch User Profile Data
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isFinishing()) return;
                        if (AccountGuard.blockIfRestricted(ProfileActivity.this, mAuth, documentSnapshot)) {
                            return;
                        }
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String course = documentSnapshot.getString("course");
                            String year = documentSnapshot.getString("year");
                            String school = documentSnapshot.getString("school");
                            String studentId = documentSnapshot.getString("studentId");
                            String role = documentSnapshot.getString("role");

                            if (fullName != null && !fullName.isEmpty()) {
                                tvFullName.setText(fullName);
                            }

                            String gender = documentSnapshot.getString("gender");
                            boolean isAdmin = AccountGuard.isAdmin(email, role);

                            if (isAdmin) {
                                tvInitials.setText("👔");
                            } else if ("female".equalsIgnoreCase(gender)) {
                                tvInitials.setText("👩‍🎓");
                            } else {
                                tvInitials.setText("👨‍🎓");
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

                            if (AccountGuard.isAdmin(email, role)) {
                                if (cardAdmin != null) cardAdmin.setVisibility(View.VISIBLE);
                                View rowOffline = findViewById(R.id.rowOffline);
                                if (rowOffline != null) rowOffline.setVisibility(View.GONE);
                                View rowReminders = findViewById(R.id.rowReminders);
                                if (rowReminders != null) rowReminders.setVisibility(View.GONE);
                                View viewReminderDivider = findViewById(R.id.viewReminderDivider);
                                if (viewReminderDivider != null) viewReminderDivider.setVisibility(View.GONE);

                                BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
                                if (bottomNav != null) {
                                    bottomNav.getMenu().clear();
                                    bottomNav.inflateMenu(R.menu.admin_bottom_nav_menu);
                                    bottomNav.setSelectedItemId(R.id.nav_profile);
                                }
                            }
                        }
                    });
        }
    }
}