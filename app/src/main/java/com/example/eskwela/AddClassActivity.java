package com.example.eskwela;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AddClassActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String selectedDate;
    private Spinner spinnerClassDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_class_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Retrieve the date passed from MyScheduleActivity
        selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        if (selectedDate == null || selectedDate.isEmpty() || "Unknown Date".equals(selectedDate)) {
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }

        // Bind Views
        EditText etClassName = findViewById(R.id.etClassName);
        EditText etClassCode = findViewById(R.id.etClassCode);
        EditText etSchool = findViewById(R.id.etAddSchool);
        spinnerClassDay = findViewById(R.id.spinnerClassDay);
        EditText etStartTime = findViewById(R.id.etStartTime);
        EditText etEndTime = findViewById(R.id.etEndTime);
        EditText etRoom = findViewById(R.id.etRoom);
        EditText etInstructor = findViewById(R.id.etInstructor);
        Button btnAddClassSubmit = findViewById(R.id.btnAddClassSubmit);

        // Setup Spinner Adapter for Day of Week
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[] {
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
                "Monday / Thursday", "Tuesday / Friday", "Monday / Wednesday / Friday", "Tuesday / Thursday"
        });
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClassDay.setAdapter(dayAdapter);

        setupTimePickers(etStartTime, etEndTime);

        btnAddClassSubmit.setOnClickListener(v -> {
            String className = etClassName.getText().toString().trim();
            String startTime = etStartTime.getText().toString().trim();
            String endTime = etEndTime.getText().toString().trim();

            if (className.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Class Name, Start Time, and End Time are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) {
                AccountGuard.goLogin(this);
                return;
            }
            String userId = mAuth.getCurrentUser().getUid();
            btnAddClassSubmit.setEnabled(false);

            String selectedDayChoice = spinnerClassDay.getSelectedItem().toString();
            String targetDate = calculateDateForDayOfWeek(selectedDayChoice, selectedDate);

            // Randomly assign a color type (Blue, Orange, Green) for the UI
            String[] colors = {"BLUE", "ORANGE", "GREEN"};
            String randomColor = colors[new Random().nextInt(colors.length)];

            // Create Data Map
            Map<String, Object> classData = new HashMap<>();
            classData.put("userId", userId);
            classData.put("dateString", targetDate);
            classData.put("className", className);
            classData.put("classCode", etClassCode.getText().toString().trim());
            classData.put("school", etSchool.getText().toString().trim());
            classData.put("startTime", startTime);
            classData.put("endTime", endTime);
            classData.put("room", etRoom.getText().toString().trim());
            classData.put("instructor", etInstructor.getText().toString().trim() + " (" + selectedDayChoice + ")");
            classData.put("colorType", randomColor);

            // Check for schedule time conflicts before saving
            checkConflictAndSave(userId, targetDate, classData, startTime, endTime, btnAddClassSubmit);
        });
    }

    private String calculateDateForDayOfWeek(String dayChoice, String fallbackDate) {
        try {
            Calendar cal = Calendar.getInstance();
            String lower = dayChoice.toLowerCase();
            if (lower.contains("tuesday")) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            } else if (lower.contains("wednesday")) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            } else if (lower.contains("thursday")) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
            } else if (lower.contains("friday")) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            } else if (lower.contains("saturday")) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            } else if (lower.contains("sunday")) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            } else {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            }
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
        } catch (Exception e) {
            return fallbackDate;
        }
    }

    private void checkConflictAndSave(String userId, String dateStr, Map<String, Object> classData, String newStart, String newEnd, Button submitBtn) {
        db.collection("schedules")
                .whereEqualTo("userId", userId)
                .whereEqualTo("dateString", dateStr)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int newStartMin = parseTimeToMinutes(newStart);
                    int newEndMin = parseTimeToMinutes(newEnd);

                    String conflictingClass = null;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String exStart = doc.getString("startTime");
                        String exEnd = doc.getString("endTime");
                        String exName = doc.getString("className");

                        if (exStart != null && exEnd != null && !exStart.isEmpty() && !exEnd.isEmpty()) {
                            int exStartMin = parseTimeToMinutes(exStart);
                            int exEndMin = parseTimeToMinutes(exEnd);

                            // Check overlap condition: max(start1, start2) < min(end1, end2)
                            if (Math.max(newStartMin, exStartMin) < Math.min(newEndMin, exEndMin)) {
                                conflictingClass = exName + " (" + exStart + " - " + exEnd + ")";
                                break;
                            }
                        }
                    }

                    if (conflictingClass != null) {
                        submitBtn.setEnabled(true);
                        new AlertDialog.Builder(this)
                                .setTitle("⚠️ Schedule Conflict Detected")
                                .setMessage("This class time overlaps with an existing scheduled class on this day:\n\n• " + conflictingClass + "\n\nWould you like to override and add this class anyway?")
                                .setPositiveButton("Override & Add", (dialog, which) -> executeSave(classData, submitBtn))
                                .setNegativeButton("Cancel / Edit Time", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
                        executeSave(classData, submitBtn);
                    }
                })
                .addOnFailureListener(e -> executeSave(classData, submitBtn));
    }

    private int parseTimeToMinutes(String timeStr) {
        try {
            String clean = timeStr.trim().toUpperCase();
            boolean isPm = clean.contains("PM") || clean.contains("P");
            boolean isAm = clean.contains("AM") || clean.contains("A");

            String digits = clean.replaceAll("[^0-9:]", "");
            String[] hm = digits.split(":");
            int hour = Integer.parseInt(hm[0]);
            int minute = hm.length > 1 ? Integer.parseInt(hm[1]) : 0;

            if (isPm && hour < 12) {
                hour += 12;
            } else if (isAm && hour == 12) {
                hour = 0;
            }
            return hour * 60 + minute;
        } catch (Exception e) {
            return 0;
        }
    }

    private void executeSave(Map<String, Object> classData, Button submitBtn) {
        db.collection("schedules").add(classData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Class Added Successfully! 📅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    submitBtn.setEnabled(true);
                    Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setupTimePickers(EditText etStartTime, EditText etEndTime) {
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
    }

    private void showTimePicker(EditText targetEditText) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String amPm = (hourOfDay >= 12) ? "PM" : "AM";
            int formattedHour = hourOfDay % 12;
            if (formattedHour == 0) formattedHour = 12;
            String timeString = String.format(Locale.getDefault(), "%d:%02d %s", formattedHour, minuteOfHour, amPm);
            targetEditText.setText(timeString);
        }, hour, minute, false);

        timePickerDialog.show();
    }
}
