package com.example.eskwela;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NotificationsActivity extends AppCompatActivity {

    // Keep track of the state
    private boolean isClassChecked = true;
    private boolean isTaskChecked = true;
    private boolean isStudyChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notif_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the new TextView checkboxes
        TextView tvClassReminders = findViewById(R.id.tvClassReminders);
        TextView tvTaskDeadlines = findViewById(R.id.tvTaskDeadlines);
        TextView tvStudyReminder = findViewById(R.id.tvStudyReminder);

        // Click Listeners to toggle the drawable icons
        tvClassReminders.setOnClickListener(v -> {
            isClassChecked = !isClassChecked;
            updateCheckboxState(tvClassReminders, isClassChecked);
        });

        tvTaskDeadlines.setOnClickListener(v -> {
            isTaskChecked = !isTaskChecked;
            updateCheckboxState(tvTaskDeadlines, isTaskChecked);
        });

        tvStudyReminder.setOnClickListener(v -> {
            isStudyChecked = !isStudyChecked;
            updateCheckboxState(tvStudyReminder, isStudyChecked);
        });


        // Initialize Segmented Control views
        TextView segDay = findViewById(R.id.segDay);
        TextView seg15Min = findViewById(R.id.seg15Min);
        TextView segAtTime = findViewById(R.id.segAtTime);

        Button btnAllow = findViewById(R.id.btnAllowNotifications);
        TextView tvSkip = findViewById(R.id.tvSkipForNow);

        // Segmented Control Click Logic
        segDay.setOnClickListener(v -> {
            segDay.setBackgroundResource(R.drawable.bg_segment_active);
            segDay.setTextColor(ContextCompat.getColor(this, R.color.eskwela_text_blue));

            seg15Min.setBackgroundResource(0);
            seg15Min.setTextColor(0xFF475569);

            segAtTime.setBackgroundResource(0);
            segAtTime.setTextColor(0xFF475569);
        });

        seg15Min.setOnClickListener(v -> {
            seg15Min.setBackgroundResource(R.drawable.bg_segment_active);
            seg15Min.setTextColor(ContextCompat.getColor(this, R.color.eskwela_text_blue));

            segDay.setBackgroundResource(0);
            segDay.setTextColor(0xFF475569);

            segAtTime.setBackgroundResource(0);
            segAtTime.setTextColor(0xFF475569);
        });

        segAtTime.setOnClickListener(v -> {
            segAtTime.setBackgroundResource(R.drawable.bg_segment_active);
            segAtTime.setTextColor(ContextCompat.getColor(this, R.color.eskwela_text_blue));

            segDay.setBackgroundResource(0);
            segDay.setTextColor(0xFF475569);

            seg15Min.setBackgroundResource(0);
            seg15Min.setTextColor(0xFF475569);
        });

        // Continue Actions
        btnAllow.setOnClickListener(v -> {
            // Navigate to ScheduleActivity
            android.content.Intent intent = new android.content.Intent(NotificationsActivity.this, ScheduleActivity.class);
            startActivity(intent);
            finish();
        });

        tvSkip.setOnClickListener(v -> {
            // Navigate to ScheduleActivity
            android.content.Intent intent = new android.content.Intent(NotificationsActivity.this, ScheduleActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Helper method to change the drawable icon
    private void updateCheckboxState(TextView textView, boolean isChecked) {
        if (isChecked) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.checkbox_checked, 0, 0, 0);
        } else {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.checkbox_unchecked, 0, 0, 0);
        }
    }
}