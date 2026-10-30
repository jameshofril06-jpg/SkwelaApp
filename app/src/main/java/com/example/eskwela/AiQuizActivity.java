package com.example.eskwela;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiQuizActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etQuizTopic;
    private TextView btnCount5, btnCount10, btnCount15;
    private View cardActiveQuiz;
    private TextView tvQuizProgress, tvQuestionText, tvLeaderboardContent, tvHistoryContent;
    private RadioGroup rgOptions;
    private RadioButton rbOption0, rbOption1, rbOption2, rbOption3;
    private Button btnSubmitAnswer;

    private int selectedQuestionCount = 5;
    private List<Question> generatedQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int scoreCount = 0;
    private String currentTopic = "";

    public static class Question {
        public String questionText;
        public String[] options;
        public int correctOptionIndex;

        public Question(String questionText, String[] options, int correctOptionIndex) {
            this.questionText = questionText;
            this.options = options;
            this.correctOptionIndex = correctOptionIndex;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_quiz);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ai_quiz_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etQuizTopic = findViewById(R.id.etQuizTopic);
        btnCount5 = findViewById(R.id.btnCount5);
        btnCount10 = findViewById(R.id.btnCount10);
        btnCount15 = findViewById(R.id.btnCount15);
        Button btnGenerateQuiz = findViewById(R.id.btnGenerateQuiz);

        cardActiveQuiz = findViewById(R.id.cardActiveQuiz);
        tvQuizProgress = findViewById(R.id.tvQuizProgress);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        rgOptions = findViewById(R.id.rgOptions);
        rbOption0 = findViewById(R.id.rbOption0);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);

        tvLeaderboardContent = findViewById(R.id.tvLeaderboardContent);
        tvHistoryContent = findViewById(R.id.tvHistoryContent);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        // Question Count Selection
        btnCount5.setOnClickListener(v -> selectQuestionCount(5));
        btnCount10.setOnClickListener(v -> selectQuestionCount(10));
        btnCount15.setOnClickListener(v -> selectQuestionCount(15));

        btnGenerateQuiz.setOnClickListener(v -> startAiQuiz());
        btnSubmitAnswer.setOnClickListener(v -> processAnswerAndNext());

        loadLeaderboardAndHistory();
    }

    private void selectQuestionCount(int count) {
        selectedQuestionCount = count;
        btnCount5.setBackgroundResource(count == 5 ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnCount5.setTextColor(count == 5 ? 0xFF1D4ED8 : 0xFF475569);

        btnCount10.setBackgroundResource(count == 10 ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnCount10.setTextColor(count == 10 ? 0xFF1D4ED8 : 0xFF475569);

        btnCount15.setBackgroundResource(count == 15 ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnCount15.setTextColor(count == 15 ? 0xFF1D4ED8 : 0xFF475569);
    }

    private void startAiQuiz() {
        currentTopic = etQuizTopic.getText().toString().trim();
        if (currentTopic.isEmpty()) {
            Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show();
            return;
        }

        generatedQuestions = generateSmartQuestions(currentTopic, selectedQuestionCount);
        currentQuestionIndex = 0;
        scoreCount = 0;

        cardActiveQuiz.setVisibility(View.VISIBLE);
        displayQuestion(currentQuestionIndex);
    }

    private void displayQuestion(int index) {
        if (index >= generatedQuestions.size()) return;
        Question q = generatedQuestions.get(index);

        tvQuizProgress.setText("Question " + (index + 1) + " of " + generatedQuestions.size() + " (" + currentTopic + ")");
        tvQuestionText.setText(q.questionText);

        rbOption0.setText(q.options[0]);
        rbOption1.setText(q.options[1]);
        rbOption2.setText(q.options[2]);
        rbOption3.setText(q.options[3]);

        rgOptions.clearCheck();
        btnSubmitAnswer.setText((index == generatedQuestions.size() - 1) ? "Submit Final Quiz" : "Next Question ➔");
    }

    private void processAnswerAndNext() {
        int checkedId = rgOptions.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = 0;
        if (checkedId == R.id.rbOption1) selectedIndex = 1;
        else if (checkedId == R.id.rbOption2) selectedIndex = 2;
        else if (checkedId == R.id.rbOption3) selectedIndex = 3;

        Question currentQ = generatedQuestions.get(currentQuestionIndex);
        if (selectedIndex == currentQ.correctOptionIndex) {
            scoreCount++;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < generatedQuestions.size()) {
            displayQuestion(currentQuestionIndex);
        } else {
            finishQuizAndSaveRecord();
        }
    }

    private void finishQuizAndSaveRecord() {
        cardActiveQuiz.setVisibility(View.GONE);
        int total = generatedQuestions.size();
        int percent = Math.round(((float) scoreCount / total) * 100);

        String resultMsg = "Quiz Complete!\n\nScore: " + scoreCount + " / " + total + " (" + percent + "%)";
        new AlertDialog.Builder(this)
                .setTitle("🎉 Quiz Results")
                .setMessage(resultMsg)
                .setPositiveButton("Awesome!", (dialog, which) -> dialog.dismiss())
                .show();

        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();

        Map<String, Object> quizRecord = new HashMap<>();
        quizRecord.put("userId", userId);
        quizRecord.put("userEmail", userEmail != null ? userEmail : "Student");
        quizRecord.put("topic", currentTopic);
        quizRecord.put("score", scoreCount);
        quizRecord.put("totalQuestions", total);
        quizRecord.put("percentage", percent);
        quizRecord.put("timestamp", System.currentTimeMillis());

        db.collection("ai_quizzes").add(quizRecord)
                .addOnSuccessListener(ref -> loadLeaderboardAndHistory());
    }

    private List<Question> generateSmartQuestions(String topic, int count) {
        List<Question> questions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String qText = "Q" + i + ": What is a fundamental concept regarding " + topic + "?";
            String[] options = new String[]{
                    "Key principle " + i + " governing " + topic,
                    "Alternative secondary theory in " + topic,
                    "Historical background parameter " + i,
                    "Irrelevant option"
            };
            questions.add(new Question(qText, options, 0));
        }
        return questions;
    }

    private void loadLeaderboardAndHistory() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // 1. Load History
        db.collection("ai_quizzes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String top = doc.getString("topic");
                            Long score = doc.getLong("score");
                            Long total = doc.getLong("totalQuestions");
                            Long pct = doc.getLong("percentage");

                            if (top != null && score != null && total != null) {
                                sb.append("• ").append(top).append(": ").append(score).append("/").append(total)
                                        .append(" (").append(pct != null ? pct : 0).append("%)\n");
                            }
                        }
                        tvHistoryContent.setText(sb.toString().trim());
                    } else {
                        tvHistoryContent.setText("No quiz history yet. Take a quiz above!");
                    }
                });

        // 2. Load Leaderboard (Top Scores across users)
        db.collection("ai_quizzes")
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

                            sb.append(medal).append(name).append(" - ").append(pct != null ? pct : 0)
                                    .append("% on ").append(topic != null ? topic : "Quiz").append("\n");
                            rank++;
                        }
                        tvLeaderboardContent.setText(sb.toString().trim());
                    } else {
                        tvLeaderboardContent.setText("1st Place 🥇 Student - 100% on Practice Quiz");
                    }
                });
    }
}
