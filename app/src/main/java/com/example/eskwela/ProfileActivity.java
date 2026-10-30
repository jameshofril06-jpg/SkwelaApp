package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
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

        // Setup OTA Update Click Listener
        findViewById(R.id.rowUpdate).setOnClickListener(v -> UpdateHelper.downloadAndInstallUpdate(this));

        // Setup Bottom Navigation
        setupBottomNavigation();
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

            // 1. Fetch User Profile Data
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String course = documentSnapshot.getString("course");
                            String year = documentSnapshot.getString("year");
                            String school = documentSnapshot.getString("school");
                            String studentId = documentSnapshot.getString("studentId");

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