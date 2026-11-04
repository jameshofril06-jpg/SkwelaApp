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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StudyActivity extends AppCompatActivity {

    private TextView tvTimer;
    private Button btnStartFocus;
    private CountDownTimer countDownTimer;

    private boolean isTimerRunning = false;
    private long selectedStartTimeInMillis = 25 * 60 * 1000L; // 25 minutes default
    private long timeLeftInMillis = 25 * 60 * 1000L;

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

        setupTimerPresets();
        setupCards();
        setupLeaderboard();
        setupBottomNavigation();
    }

    private void setupTimerPresets() {
        TextView p2 = findViewById(R.id.btnPreset2);
        TextView p5 = findViewById(R.id.btnPreset5);
        TextView p10 = findViewById(R.id.btnPreset10);
        TextView p15 = findViewById(R.id.btnPreset15);
        TextView p25 = findViewById(R.id.btnPreset25);
        TextView p45 = findViewById(R.id.btnPreset45);
        TextView p60 = findViewById(R.id.btnPreset60);
        Button btnReset = findViewById(R.id.btnResetFocus);

        if (p2 != null) p2.setOnClickListener(v -> selectPresetMinutes(2));
        if (p5 != null) p5.setOnClickListener(v -> selectPresetMinutes(5));
        if (p10 != null) p10.setOnClickListener(v -> selectPresetMinutes(10));
        if (p15 != null) p15.setOnClickListener(v -> selectPresetMinutes(15));
        if (p25 != null) p25.setOnClickListener(v -> selectPresetMinutes(25));
        if (p45 != null) p45.setOnClickListener(v -> selectPresetMinutes(45));
        if (p60 != null) p60.setOnClickListener(v -> selectPresetMinutes(60));

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetTimer());
        }
    }

    private void selectPresetMinutes(int minutes) {
        if (isTimerRunning) {
            Toast.makeText(this, "Pause or reset timer before changing duration", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedStartTimeInMillis = minutes * 60 * 1000L;
        timeLeftInMillis = selectedStartTimeInMillis;
        updateCountDownText();
        Toast.makeText(this, "Timer set to " + minutes + " minutes ⏱️", Toast.LENGTH_SHORT).show();
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timeLeftInMillis = selectedStartTimeInMillis;
        updateCountDownText();
        btnStartFocus.setText("Start focus");
    }

    private void setupLeaderboard() {
        TextView tvTakeQuiz = findViewById(R.id.tvTakeQuizLeaderboard);
        CardView cardLeaderboard = findViewById(R.id.cardStudyLeaderboard);
        TextView tvScores = findViewById(R.id.tvLeaderboardScores);

        if (tvTakeQuiz != null) {
            tvTakeQuiz.setOnClickListener(v -> startActivity(new Intent(StudyActivity.this, AiQuizActivity.class)));
        }
        if (cardLeaderboard != null) {
            cardLeaderboard.setOnClickListener(v -> startActivity(new Intent(StudyActivity.this, AiQuizActivity.class)));
        }

        FirebaseFirestore.getInstance().collection("ai_quizzes")
                .orderBy("percentage", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        int rank = 1;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String email = doc.getString("userEmail");
                            String topic = doc.getString("topic");
                            Long pct = doc.getLong("percentage");

                            String medal = rank == 1 ? "🥇 " : rank == 2 ? "🥈 " : rank == 3 ? "🥉 " : rank + ". ";
                            String name = email != null ? email.split("@")[0] : "Student";

                            sb.append(medal).append(name).append(" — ").append(pct != null ? pct : 0)
                                    .append("% on ").append(topic != null ? topic : "Quiz").append("\n");
                            rank++;
                        }
                        if (tvScores != null) tvScores.setText(sb.toString().trim());
                    } else {
                        if (tvScores != null) tvScores.setText("🥇 Student — 100% on LOAD TESTING\n🥈 Student — 90% on SOFTWARE ENGINEERING\n🥉 Student — 80% on CAPSTONE PROJECT 1");
                    }
                });
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
                timeLeftInMillis = selectedStartTimeInMillis;
                updateCountDownText();
                btnStartFocus.setText("Start focus");
                Toast.makeText(StudyActivity.this, "🎉 Focus session complete! Great work!", Toast.LENGTH_LONG).show();
                recordCompletedSession(selectedStartTimeInMillis / (60 * 1000L));
            }
        }.start();

        isTimerRunning = true;
        btnStartFocus.setText("Pause");
    }

    private void recordCompletedSession(long minutes) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> session = new HashMap<>();
        session.put("userId", userId);
        session.put("durationMinutes", minutes);
        session.put("completedAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("focus_sessions").add(session);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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
    protected void onResume() {
        super.onResume();
        setupLeaderboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void setupCards() {
        CardView cardAI = findViewById(R.id.cardAI);
        CardView cardNotes = findViewById(R.id.cardNotes);

        cardAI.setOnClickListener(v -> {
            startActivity(new Intent(StudyActivity.this, AiQuizActivity.class));
        });

        cardNotes.setOnClickListener(v ->
                Toast.makeText(this, "Study Notes coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_study);

        bottomNav.setOnItemSelectedListener(item -> {
            int targetIndex = NavigationHelper.getTabIndex(item.getItemId());
            Class<?> targetClass = null;
            if (targetIndex == 3) return true;
            if (targetIndex == 0) targetClass = DashboardActivity.class;
            else if (targetIndex == 1) targetClass = MyScheduleActivity.class;
            else if (targetIndex == 2) targetClass = TasksActivity.class;
            else if (targetIndex == 4) targetClass = ProfileActivity.class;

            if (targetClass != null) {
                NavigationHelper.navigateWithTabAnimation(this, 3, targetIndex, targetClass);
                return true;
            }
            return false;
        });
    }
}
