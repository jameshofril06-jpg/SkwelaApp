package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TasksActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private TextView tvSubtitle;
    private RecyclerView rvTasks;
    private TextView tvEmptyTasks, tabAll, tabToDo, tabDone;
    private CardView cardAddTaskForm;
    private EditText etNewTaskName;

    private TaskAdapter adapter;
    private List<TaskAdapter.TaskItem> allTasks = new ArrayList<>();
    private List<TaskAdapter.TaskItem> displayedTasks = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String currentFilter = "ALL";
    private boolean isFormVisible = false;
    private boolean isDataLoaded = false; // Prevents double-loading on resume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tasks);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tasks_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvSubtitle = findViewById(R.id.tvTasksSubtitle);
        rvTasks = findViewById(R.id.rvTasks);
        tvEmptyTasks = findViewById(R.id.tvEmptyTasks);
        cardAddTaskForm = findViewById(R.id.cardAddTaskForm);
        etNewTaskName = findViewById(R.id.etNewTaskName);
        Button btnCreateTask = findViewById(R.id.btnCreateTask);

        tabAll = findViewById(R.id.tabAll);
        tabToDo = findViewById(R.id.tabToDo);
        tabDone = findViewById(R.id.tabDone);

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(displayedTasks, this);
        rvTasks.setAdapter(adapter);

        setupFilters();
        setupBottomNavigation();

        CardView btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(v -> toggleAddForm());
        btnCreateTask.setOnClickListener(v -> saveNewTask());

        etNewTaskName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveNewTask();
                return true;
            }
            return false;
        });

        // Initial Load
        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FIX: Only reload from Firestore if we haven't loaded yet (e.g. coming from another Activity).
        // This prevents the "unchecking" bug when navigating.
        if (!isDataLoaded) {
            loadTasks();
        }
    }

    private void loadTasks() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allTasks.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TaskAdapter.TaskItem item = document.toObject(TaskAdapter.TaskItem.class);
                            item.id = document.getId();
                            allTasks.add(item);
                        }
                        isDataLoaded = true; // Mark as loaded
                        applyFilter();
                    }
                });
    }

    // --- CRITICAL FIX: Update ONLY local UI, do NOT reload from Firestore ---
    @Override
    public void onTaskCheck(TaskAdapter.TaskItem task, int position) {
        boolean newStatus = !task.completed;

        // 1. Update local model object
        task.completed = newStatus;

        // 2. Update UI instantly for this specific row
        adapter.notifyItemChanged(position);

        // 3. Firestore update in the background
        db.collection("tasks").document(task.id)
                .update("completed", newStatus)
                .addOnFailureListener(e -> {
                    // Revert if Firebase fails
                    task.completed = !newStatus;
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Error syncing to database", Toast.LENGTH_SHORT).show();
                });

        // 4. Update header counter
        updateSubtitleCount();
    }

    @Override
    public void onTaskDelete(TaskAdapter.TaskItem task, int position) {
        db.collection("tasks").document(task.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    allTasks.remove(task);
                    displayedTasks.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, displayedTasks.size());
                    updateSubtitleCount();
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateSubtitleCount() {
        int pendingCount = 0;
        for (TaskAdapter.TaskItem t : allTasks) {
            if (!t.completed) pendingCount++;
        }
        tvSubtitle.setText(pendingCount + " things to finish");
        tvEmptyTasks.setVisibility(displayedTasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void toggleAddForm() {
        if (isFormVisible) {
            cardAddTaskForm.setVisibility(View.GONE);
            etNewTaskName.setText("");
            isFormVisible = false;
        } else {
            cardAddTaskForm.setVisibility(View.VISIBLE);
            cardAddTaskForm.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
            etNewTaskName.requestFocus();
            isFormVisible = true;
        }
    }

    private void saveNewTask() {
        String taskName = etNewTaskName.getText().toString().trim();
        if (taskName.isEmpty()) return;

        String userId = mAuth.getCurrentUser().getUid();
        SimpleDateFormat displayDate = new SimpleDateFormat("MMM d · h:mm a", Locale.getDefault());
        String currentDate = "Today · " + displayDate.format(new Date());

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("userId", userId);
        taskData.put("title", taskName);
        taskData.put("dateString", currentDate);
        taskData.put("completed", false); // Save as false initially

        db.collection("tasks").add(taskData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task Created!", Toast.LENGTH_SHORT).show();
                    toggleAddForm();
                    setFilter("ALL");
                    isDataLoaded = false; // Force reload so new item shows
                    loadTasks();
                });
    }

    private void setupFilters() {
        tabAll.setOnClickListener(v -> setFilter("ALL"));
        tabToDo.setOnClickListener(v -> setFilter("TODO"));
        tabDone.setOnClickListener(v -> setFilter("DONE"));
    }

    private void setFilter(String filter) {
        currentFilter = filter;

        tabAll.setBackgroundResource(0);
        tabAll.setTextColor(ContextCompat.getColor(this, R.color.eskwela_text_blue));
        tabAll.setTypeface(null, android.graphics.Typeface.NORMAL);

        tabToDo.setBackgroundResource(0);
        tabToDo.setTextColor(ContextCompat.getColor(this, R.color.eskwela_text_blue));
        tabToDo.setTypeface(null, android.graphics.Typeface.NORMAL);

        tabDone.setBackgroundResource(0);
        tabDone.setTextColor(ContextCompat.getColor(this, R.color.eskwela_text_blue));
        tabDone.setTypeface(null, android.graphics.Typeface.NORMAL);

        TextView activeTab = filter.equals("ALL") ? tabAll : filter.equals("TODO") ? tabToDo : tabDone;
        activeTab.setBackgroundResource(R.drawable.bg_segment_active);
        activeTab.setTextColor(ContextCompat.getColor(this, R.color.eskwela_text_blue));
        activeTab.setTypeface(null, android.graphics.Typeface.BOLD);

        applyFilter();
    }

    private void applyFilter() {
        displayedTasks.clear();

        for (TaskAdapter.TaskItem task : allTasks) {
            boolean shouldAdd = false;
            if (currentFilter.equals("ALL")) shouldAdd = true;
            else if (currentFilter.equals("TODO") && !task.completed) shouldAdd = true;
            else if (currentFilter.equals("DONE") && task.completed) shouldAdd = true;

            if (shouldAdd) displayedTasks.add(task);
        }

        adapter.notifyDataSetChanged();
        updateSubtitleCount();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_tasks);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(TasksActivity.this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_schedule) {
                startActivity(new Intent(TasksActivity.this, MyScheduleActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_tasks) {
                return true;
            } else if (itemId == R.id.nav_study) {
                startActivity(new Intent(TasksActivity.this, StudyActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(TasksActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}