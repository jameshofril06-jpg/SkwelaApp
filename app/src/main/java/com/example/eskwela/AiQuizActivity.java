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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AiQuizActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etQuizTopic;
    private TextView btnCount5, btnCount10, btnCount15;
    private View cardActiveQuiz;
    private TextView tvQuizProgress, tvQuestionText, tvLeaderboardContent, tvHistoryContent;
    private RadioGroup rgOptions;
    private RadioButton rbOption0, rbOption1, rbOption2, rbOption3;
    private Button btnSubmitAnswer, btnPickStudyLoad;

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
        btnPickStudyLoad = findViewById(R.id.btnPickStudyLoad);

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
        btnPickStudyLoad.setOnClickListener(v -> pickStudyLoadCourse());

        loadLeaderboardAndHistory();
    }

    private void pickStudyLoadCourse() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("schedules").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> courses = new LinkedHashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("className");
                        if (name != null && !name.isEmpty()) {
                            courses.add(name);
                        }
                    }

                    if (courses.isEmpty()) {
                        // Default courses if schedule not yet plotted
                        courses.add("CAPSTONE PROJECT 1");
                        courses.add("LOAD TESTING");
                        courses.add("IT PRACTICUM (486 HOURS)");
                        courses.add("IT MAJOR ELECTIVE 3");
                        courses.add("READINGS IN PHILIPPINE HISTORY");
                        courses.add("SOFTWARE ENGINEERING");
                    }

                    String[] courseArray = courses.toArray(new String[0]);
                    new AlertDialog.Builder(this)
                            .setTitle("Select Enrolled Study Load Course")
                            .setItems(courseArray, (dialog, which) -> {
                                etQuizTopic.setText(courseArray[which]);
                                Toast.makeText(this, "Selected: " + courseArray[which], Toast.LENGTH_SHORT).show();
                            })
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load study load", Toast.LENGTH_SHORT).show());
    }

    private void selectQuestionCount(int count) {
        selectedQuestionCount = count;
        btnCount5.setBackgroundResource(count == 5 ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnCount5.setTextColor(count == 5 ? 0xFF1D4ED8 : 0xFFCBD5E1);

        btnCount10.setBackgroundResource(count == 10 ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnCount10.setTextColor(count == 10 ? 0xFF1D4ED8 : 0xFFCBD5E1);

        btnCount15.setBackgroundResource(count == 15 ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnCount15.setTextColor(count == 15 ? 0xFF1D4ED8 : 0xFFCBD5E1);
    }

    private void startAiQuiz() {
        currentTopic = etQuizTopic.getText().toString().trim();
        if (currentTopic.isEmpty()) {
            Toast.makeText(this, "Please enter a topic or select a course", Toast.LENGTH_SHORT).show();
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

        String resultMsg = "Quiz Complete!\n\nTopic: " + currentTopic + "\nScore: " + scoreCount + " / " + total + " (" + percent + "%)";
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
        String topicUpper = topic.toUpperCase();

        if (topicUpper.contains("LOAD TESTING") || topicUpper.contains("CS6303")) {
            questions.add(new Question("What is the primary goal of Load Testing?", new String[]{"Measure system performance under expected & peak user load", "Check UI font styles", "Test internet connection speed", "Write unit test code"}, 0));
            questions.add(new Question("What metric measures throughput in Load Testing?", new String[]{"Requests Processed Per Second (RPS)", "Disk Space Usage", "Screen Resolution", "CPU Clock Speed"}, 0));
            questions.add(new Question("Which tool is widely used for load and performance testing?", new String[]{"Apache JMeter", "Adobe Photoshop", "Microsoft Word", "VLC Player"}, 0));
            questions.add(new Question("What characterizes a Stress Test?", new String[]{"Pushing system beyond normal limits to find breaking point", "Testing only 1 user", "Checking spelling", "Formatting database"}, 0));
            questions.add(new Question("What does Latency represent in performance testing?", new String[]{"Time delay for request to travel to server and back", "File size on disk", "Total RAM capacity", "Number of lines of code"}, 0));
        } else if (topicUpper.contains("SOFTWARE ENGINEERING") || topicUpper.contains("CS6209")) {
            questions.add(new Question("What does Agile Software Engineering emphasize?", new String[]{"Iterative development and customer collaboration", "Rigid 10-year plans", "No testing", "Only manual paperwork"}, 0));
            questions.add(new Question("Which diagram models class relationships in UML?", new String[]{"Class Diagram", "Flowchart", "Pie Chart", "Network Topology"}, 0));
            questions.add(new Question("What is the goal of Refactoring?", new String[]{"Improving internal code structure without changing external behavior", "Adding new bugs", "Deleting tests", "Renaming the project"}, 0));
            questions.add(new Question("What does the Singleton pattern ensure?", new String[]{"Class has only one single instance", "Class creates 100 instances", "Class deletes database", "Class runs only on Linux"}, 0));
            questions.add(new Question("What is Regression Testing?", new String[]{"Ensuring recent code changes haven't broken existing features", "Testing hardware voltage", "Writing user manuals", "Designing logos"}, 0));
        } else if (topicUpper.contains("CAPSTONE") || topicUpper.contains("IT6398")) {
            questions.add(new Question("What is the core starting point of Capstone Project 1?", new String[]{"Problem Statement and Scope Definition", "Buying server hardware", "Choosing app icon colors", "Creating marketing ads"}, 0));
            questions.add(new Question("What assesses the technical and operational viability of capstone?", new String[]{"Feasibility Study", "Class Attendance Sheet", "Receipt Voucher", "Quiz Scoreboard"}, 0));
            questions.add(new Question("What documents existing research relevant to capstone?", new String[]{"Review of Related Literature (RRL)", "User Password List", "Class Timetable", "Device Storage"}, 0));
            questions.add(new Question("What development lifecycle is recommended for Capstone?", new String[]{"Agile / Scrum Sprint Iterations", "Waterfall with no feedback", "Ad-hoc random coding", "No documentation"}, 0));
            questions.add(new Question("What defines the boundaries of Capstone Project?", new String[]{"System Scope and Delimitation", "University Budget", "Student ID Number", "Classroom Seat Number"}, 0));
        } else if (topicUpper.contains("PHILIPPINE HISTORY") || topicUpper.contains("GE6101")) {
            questions.add(new Question("What defines a primary historical source?", new String[]{"First-hand document or artifact created during the time period", "A modern textbook written in 2024", "A fictional movie", "A blog opinion post"}, 0));
            questions.add(new Question("Who authored the Kartilya ng Katipunan?", new String[]{"Emilio Jacinto", "Jose Rizal", "Andres Bonifacio", "Apolinario Mabini"}, 0));
            questions.add(new Question("Where did the 1897 Tejeros Convention take place?", new String[]{"San Francisco de Malabon, Cavite", "Manila", "Bulacan", "Cebu"}, 0));
            questions.add(new Question("Who wrote 'Customs of the Tagalogs' in 1589?", new String[]{"Juan de Plasencia", "Antonio Pigafetta", "Emilio Aguinaldo", "Marcelo H. del Pilar"}, 0));
            questions.add(new Question("What historical event took place on January 20, 1872?", new String[]{"Cavite Mutiny", "Cry of Pugad Lawin", "Declaration of Independence", "Battle of Mactan"}, 0));
        } else if (topicUpper.contains("PRACTICUM") || topicUpper.contains("IT6397")) {
            questions.add(new Question("What is the main requirement of IT Practicum?", new String[]{"Completing 486 hours of industry workplace training", "Writing a 100-page poem", "Buying 5 computers", "Passing history quiz"}, 0));
            questions.add(new Question("Which document logs daily industry tasks during practicum?", new String[]{"Daily Time Record (DTR) & Weekly Report", "Library Card", "Food Receipt", "Bus Ticket"}, 0));
            questions.add(new Question("Which Philippine law governs workplace data privacy?", new String[]{"Data Privacy Act of 2012 (RA 10173)", "Clean Air Act", "Building Code", "Traffic Rules"}, 0));
            questions.add(new Question("What is critical for professional communication during practicum?", new String[]{"Clear documentation and mentor status updates", "Ignoring emails", "Arriving late", "Deleting code"}, 0));
            questions.add(new Question("What evaluates intern performance at industry workplace?", new String[]{"Industry Supervisor Evaluation Form", "Random Online Poll", "Self-Rating", "Classmate Vote"}, 0));
        } else {
            questions.add(new Question("What is a core fundamental principle in " + topic + "?", new String[]{"Key concept " + topic + " foundational model", "Unrelated secondary theory", "Historical assumption", "Irrelevant choice"}, 0));
            questions.add(new Question("Why is " + topic + " important in modern IT curriculum?", new String[]{"Provides practical domain competence & technical problem solving", "It has no practical application", "It is purely decorative", "Only for historical trivia"}, 0));
            questions.add(new Question("Which methodology applies best to " + topic + "?", new String[]{"Structured analysis and systematic evaluation", "Random guessing", "Ignoring requirements", "Skipping validation"}, 0));
            questions.add(new Question("What is a key deliverable when studying " + topic + "?", new String[]{"Comprehensive documentation and working technical prototype", "Empty text file", "Unformatted draft", "No output"}, 0));
            questions.add(new Question("How is success measured in " + topic + "?", new String[]{"Meeting defined objectives and domain standards", "By guessing", "Without metrics", "By luck"}, 0));
        }

        // Expand pool to match requested count (5, 10, or 15)
        List<Question> result = new ArrayList<>();
        int baseSize = questions.size();
        for (int i = 0; i < count; i++) {
            Question base = questions.get(i % baseSize);
            String qText = (count > 5) ? "(" + topic + " Q" + (i + 1) + ") " + base.questionText : base.questionText;
            result.add(new Question(qText, base.options, base.correctOptionIndex));
        }
        return result;
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
