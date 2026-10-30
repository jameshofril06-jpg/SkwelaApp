package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText etFullName, etSchool, etCourse, etYear, etSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind Views
        etFullName = findViewById(R.id.etProfFullName);
        etSchool = findViewById(R.id.etSchool);
        etCourse = findViewById(R.id.etCourse);
        etYear = findViewById(R.id.etYear);
        etSection = findViewById(R.id.etSection);
        TextView tvInitials = findViewById(R.id.tvAvatarInitials);
        Button btnContinue = findViewById(R.id.btnContinue);

        // Live initials update logic
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString().trim();
                if (name.isEmpty()) {
                    tvInitials.setText("JD");
                } else {
                    String[] parts = name.split("\\s+");
                    String initials = "";
                    if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].substring(0, 1).toUpperCase();
                    if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) initials += parts[parts.length - 1].substring(0, 1).toUpperCase();
                    tvInitials.setText(initials);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnContinue.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current user UID
            String userId = mAuth.getCurrentUser().getUid();

            // Create a Map to store user data
            Map<String, Object> user = new HashMap<>();
            user.put("fullName", fullName);
            user.put("school", etSchool.getText().toString().trim());
            user.put("course", etCourse.getText().toString().trim());
            user.put("year", etYear.getText().toString().trim());
            user.put("section", etSection.getText().toString().trim());

            // Save to Firestore using SetOptions.merge()
            // This is CRITICAL so it doesn't overwrite the studentId we generated in RegisterActivity
            db.collection("users").document(userId).set(user, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileSetupActivity.this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                        // Redirect to Notifications or directly to Dashboard
                        Intent intent = new Intent(ProfileSetupActivity.this, NotificationsActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileSetupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}