package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Random;

public class AddClassActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String selectedDate;

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
        if (selectedDate == null) selectedDate = "Unknown Date";

        // Bind Views
        EditText etClassName = findViewById(R.id.etClassName);
        EditText etClassCode = findViewById(R.id.etClassCode);
        EditText etSchool = findViewById(R.id.etAddSchool);
        EditText etStartTime = findViewById(R.id.etStartTime);
        EditText etEndTime = findViewById(R.id.etEndTime);
        EditText etRoom = findViewById(R.id.etRoom);
        EditText etInstructor = findViewById(R.id.etInstructor);
        Button btnAddClassSubmit = findViewById(R.id.btnAddClassSubmit);

        btnAddClassSubmit.setOnClickListener(v -> {
            String className = etClassName.getText().toString().trim();
            String startTime = etStartTime.getText().toString().trim();

            if (className.isEmpty() || startTime.isEmpty()) {
                Toast.makeText(this, "Class Name and Start Time are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) return;
            String userId = mAuth.getCurrentUser().getUid();

            // Randomly assign a color type (Blue, Orange, Green) for the UI
            String[] colors = {"BLUE", "ORANGE", "GREEN"};
            String randomColor = colors[new Random().nextInt(colors.length)];

            // Create Data Map
            Map<String, Object> classData = new HashMap<>();
            classData.put("userId", userId);
            classData.put("dateString", selectedDate);
            classData.put("className", className);
            classData.put("classCode", etClassCode.getText().toString().trim());
            classData.put("school", etSchool.getText().toString().trim());
            classData.put("startTime", startTime);
            classData.put("endTime", etEndTime.getText().toString().trim());
            classData.put("room", etRoom.getText().toString().trim());
            classData.put("instructor", etInstructor.getText().toString().trim());
            classData.put("colorType", randomColor);

            // Save to Firestore
            db.collection("schedules").add(classData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Class Added Successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to schedule page
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}