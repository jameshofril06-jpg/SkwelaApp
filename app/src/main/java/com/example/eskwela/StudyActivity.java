package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class StudyActivity extends AppCompatActivity {

    private TextView tvTimer;
    private Button btnStartFocus;
    private CountDownTimer countDownTimer;

    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 25 * 60 * 1000; // 25 minutes in milliseconds
    private static final long START_TIME_IN_MILLIS = 25 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.study_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        tvTimer = findViewById(R.id.tvTimer);
        btnStartFocus = findViewById(R.id.btnStartFocus);

        // Update timer display initially
        updateCountDownText();

        btnStartFocus.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        setupCards();
        setupBottomNavigation();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timeLeftInMillis = START_TIME_IN_MILLIS;
                updateCountDownText();
                btnStartFocus.setText("Start focus");
                Toast.makeText(StudyActivity.this, "Focus session complete!", Toast.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        btnStartFocus.setText("Pause");
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        btnStartFocus.setText("Resume");
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeLeftFormatted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent memory leaks by cancelling timer if activity is destroyed
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void setupCards() {
        CardView cardAI = findViewById(R.id.cardAI);
        CardView cardNotes = findViewById(R.id.cardNotes);

        cardAI.setOnClickListener(v ->
                Toast.makeText(this, "AI Quiz Generator coming soon!", Toast.LENGTH_SHORT).show()
        );

        cardNotes.setOnClickListener(v ->
                Toast.makeText(this, "Study Notes coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_study); // Highlight Study tab

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(StudyActivity.this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_schedule) {
                startActivity(new Intent(StudyActivity.this, MyScheduleActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_tasks) {
                startActivity(new Intent(StudyActivity.this, TasksActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_study) {
                return true; // Already here
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(StudyActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}