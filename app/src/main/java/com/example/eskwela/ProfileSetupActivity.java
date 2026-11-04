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
    private boolean editMode;

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

        editMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        String passedName = getIntent().getStringExtra("fullName");
        if (passedName != null && !passedName.isEmpty()) {
            etFullName.setText(passedName);
        }
        if (editMode) {
            btnContinue.setText("Save changes");
            prefillExistingProfile();
        }

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

            if (mAuth.getCurrentUser() == null) {
                AccountGuard.goLogin(this);
                return;
            }
            String userId = mAuth.getCurrentUser().getUid();

            Map<String, Object> user = new HashMap<>();
            user.put("fullName", fullName);
            user.put("school", etSchool.getText().toString().trim());
            user.put("course", etCourse.getText().toString().trim());
            user.put("year", etYear.getText().toString().trim());
            user.put("section", etSection.getText().toString().trim());

            btnContinue.setEnabled(false);
            db.collection("users").document(userId).set(user, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileSetupActivity.this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                        if (editMode) {
                            finish();
                            return;
                        }
                        Intent intent = new Intent(ProfileSetupActivity.this, NotificationsActivity.class);
                        intent.putExtra("FROM_ONBOARDING", true);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnContinue.setEnabled(true);
                        Toast.makeText(ProfileSetupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void prefillExistingProfile() {
        if (mAuth.getCurrentUser() == null) return;
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    setIfEmpty(etFullName, doc.getString("fullName"));
                    setIfEmpty(etSchool, doc.getString("school"));
                    setIfEmpty(etCourse, doc.getString("course"));
                    setIfEmpty(etYear, doc.getString("year"));
                    setIfEmpty(etSection, doc.getString("section"));
                });
    }

    private void setIfEmpty(EditText field, String value) {
        if (field != null && (field.getText() == null || field.getText().toString().trim().isEmpty())
                && value != null && !value.isEmpty()) {
            field.setText(value);
        }
    }
}