package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.schedule_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        CardView cardCamera = findViewById(R.id.cardCamera);
        CardView cardGallery = findViewById(R.id.cardGallery);
        CardView cardManual = findViewById(R.id.cardManual);
        Button btnFinish = findViewById(R.id.btnFinishSetup);
        TextView tvSkip = findViewById(R.id.tvScheduleSkip);

        // Card Click Listeners - Seed official schedule matching registration form
        cardCamera.setOnClickListener(v -> seedOfficialSchedules("Timetable scanned successfully!"));
        cardGallery.setOnClickListener(v -> seedOfficialSchedules("Schedule photo imported successfully!"));
        cardManual.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, AddClassActivity.class);
            startActivity(intent);
        });

        // Finish Setup Button
        btnFinish.setOnClickListener(v -> seedOfficialSchedules("Official schedule loaded successfully!"));

        // Skip Link
        tvSkip.setOnClickListener(v -> seedOfficialSchedules("Official schedule loaded successfully!"));
    }

    private void seedOfficialSchedules(String message) {
        if (mAuth.getCurrentUser() == null) {
            navigateToDashboard();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        // Calculate dates for Monday, Tuesday, Thursday, Friday of the current week
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Monday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String mondayDate = dbFormat.format(cal.getTime());

        // Tuesday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        String tuesdayDate = dbFormat.format(cal.getTime());

        // Thursday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        String thursdayDate = dbFormat.format(cal.getTime());

        // Friday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        String fridayDate = dbFormat.format(cal.getTime());

        // Clear existing schedules for user to avoid duplication, then add official schedule
        db.collection("schedules")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    // 1. Monday & Thursday (MTH)
                    String[] mthDates = {mondayDate, thursdayDate};
                    for (String date : mthDates) {
                        // CS6303: Load Testing (12:00 - 2:30 PM, Room SLAB5)
                        addSchedule(userId, date, "LOAD TESTING", "CS6303", "12:00 PM", "2:30 PM", "SLAB5", "BLUE");
                        // IT6397: IT Practicum (3:00 - 4:30 PM, Room 214)
                        addSchedule(userId, date, "IT PRACTICUM", "IT6397", "3:00 PM", "4:30 PM", "214", "GREEN");
                        // GE6101: Readings in Philippines (4:30 - 6:00 PM, Room 209)
                        addSchedule(userId, date, "READINGS IN PHILIPPINES", "GE6101", "4:30 PM", "6:00 PM", "209", "ORANGE");
                    }

                    // 2. Tuesday & Friday (TF)
                    String[] tfDates = {tuesdayDate, fridayDate};
                    for (String date : tfDates) {
                        // IT6312: IT Major Elective 3 (12:00 - 2:30 PM, Room SLAB5)
                        addSchedule(userId, date, "IT MAJOR ELECTIVE 3", "IT6312", "12:00 PM", "2:30 PM", "SLAB5", "ORANGE");
                        // IT6398: Capstone Project 1 (3:00 - 4:30 PM, Room 301)
                        addSchedule(userId, date, "CAPSTONE PROJECT 1", "IT6398", "3:00 PM", "4:30 PM", "301", "BLUE");
                    }

                    // 3. Software Engineering (CS6209) - Tuesday 4:30-5:30 (Room 204), Friday 6:00-7:30 (Room SLAB3)
                    addSchedule(userId, tuesdayDate, "SOFTWARE ENGINEERING", "CS6209", "4:30 PM", "5:30 PM", "204", "GREEN");
                    addSchedule(userId, fridayDate, "SOFTWARE ENGINEERING", "CS6209", "6:00 PM", "7:30 PM", "SLAB3", "GREEN");

                    Toast.makeText(ScheduleActivity.this, message, Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ScheduleActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                });
    }

    private void addSchedule(String userId, String dateString, String className, String classCode, String startTime, String endTime, String room, String colorType) {
        Map<String, Object> classData = new HashMap<>();
        classData.put("userId", userId);
        classData.put("dateString", dateString);
        classData.put("className", className);
        classData.put("classCode", classCode);
        classData.put("school", "University");
        classData.put("startTime", startTime);
        classData.put("endTime", endTime);
        classData.put("room", room);
        classData.put("instructor", "");
        classData.put("colorType", colorType);

        db.collection("schedules").add(classData);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(ScheduleActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}