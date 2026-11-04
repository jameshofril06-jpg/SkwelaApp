package com.example.eskwela;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
    private EditText etNewTaskName, etTaskCourseName;
    private TextView btnPriorityHigh, btnPriorityMed, btnPriorityLow;

    private TaskAdapter adapter;
    private List<TaskAdapter.TaskItem> allTasks = new ArrayList<>();
    private List<TaskAdapter.TaskItem> displayedTasks = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String currentFilter = "ALL";
    private String selectedPriority = "MEDIUM";
    private boolean isFormVisible = false;
    private boolean isDataLoaded = false;

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
        etTaskCourseName = findViewById(R.id.etTaskCourseName);
        Button btnCreateTask = findViewById(R.id.btnCreateTask);

        btnPriorityHigh = findViewById(R.id.btnPriorityHigh);
        btnPriorityMed = findViewById(R.id.btnPriorityMed);
        btnPriorityLow = findViewById(R.id.btnPriorityLow);

        tabAll = findViewById(R.id.tabAll);
        tabToDo = findViewById(R.id.tabToDo);
        tabDone = findViewById(R.id.tabDone);

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(displayedTasks, this);
        rvTasks.setAdapter(adapter);

        setupFilters();
        setupBottomNavigation();

        ImageView btnAddTaskToggle = findViewById(R.id.btnAddTaskToggle);
        if (btnAddTaskToggle != null) btnAddTaskToggle.setOnClickListener(v -> toggleAddForm());
        btnCreateTask.setOnClickListener(v -> saveNewTask());

        if (btnPriorityHigh != null) btnPriorityHigh.setOnClickListener(v -> selectPriority("HIGH"));
        if (btnPriorityMed != null) btnPriorityMed.setOnClickListener(v -> selectPriority("MEDIUM"));
        if (btnPriorityLow != null) btnPriorityLow.setOnClickListener(v -> selectPriority("LOW"));

        etNewTaskName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveNewTask();
                return true;
            }
            return false;
        });

        loadTasks();
    }

    private void selectPriority(String priority) {
        selectedPriority = priority;
        if (btnPriorityHigh != null) {
            btnPriorityHigh.setBackgroundResource("HIGH".equals(priority) ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        }
        if (btnPriorityMed != null) {
            btnPriorityMed.setBackgroundResource("MEDIUM".equals(priority) ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        }
        if (btnPriorityLow != null) {
            btnPriorityLow.setBackgroundResource("LOW".equals(priority) ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        isDataLoaded = true;
                        applyFilter();
                    }
                });
    }

    @Override
    public void onTaskCheck(TaskAdapter.TaskItem task, int position) {
        boolean newStatus = !task.completed;
        task.completed = newStatus;
        adapter.notifyItemChanged(position);

        db.collection("tasks").document(task.id)
                .update("completed", newStatus)
                .addOnFailureListener(e -> {
                    task.completed = !newStatus;
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Error syncing to database", Toast.LENGTH_SHORT).show();
                });

        updateSubtitleCount();
        applyFilter();
    }

    @Override
    public void onTaskDelete(TaskAdapter.TaskItem task, int position) {
        EditText etReason = new EditText(this);
        etReason.setHint("Enter reason to drop task...");
        etReason.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle("Drop / Delete Task")
                .setMessage("Are you sure you want to drop task: " + (task.title != null ? task.title : "Task") + "?")
                .setView(etReason)
                .setPositiveButton("Drop Task", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    db.collection("tasks").document(task.id)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                allTasks.remove(task);
                                if (position >= 0 && position < displayedTasks.size()) {
                                    displayedTasks.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, displayedTasks.size());
                                } else {
                                    applyFilter();
                                }
                                updateSubtitleCount();
                                Toast.makeText(this, "Task dropped successfully. Reason: " + (reason.isEmpty() ? "None specified" : reason), Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Could not drop task", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
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
            if (etTaskCourseName != null) etTaskCourseName.setText("");
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

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        SimpleDateFormat displayDate = new SimpleDateFormat("MMM d · h:mm a", Locale.getDefault());
        String currentDate = "Today · " + displayDate.format(new Date());

        String courseName = etTaskCourseName != null ? etTaskCourseName.getText().toString().trim() : "";

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("userId", userId);
        taskData.put("title", taskName);
        taskData.put("dateString", currentDate);
        taskData.put("courseName", courseName.isEmpty() ? "General" : courseName);
        taskData.put("priority", selectedPriority);
        taskData.put("completed", false);

        db.collection("tasks").add(taskData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task Created!", Toast.LENGTH_SHORT).show();
                    toggleAddForm();
                    setFilter("ALL");
                    isDataLoaded = false;
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
            int targetIndex = NavigationHelper.getTabIndex(item.getItemId());
            Class<?> targetClass = null;
            if (targetIndex == 2) return true;
            if (targetIndex == 0) targetClass = DashboardActivity.class;
            else if (targetIndex == 1) targetClass = MyScheduleActivity.class;
            else if (targetIndex == 3) targetClass = StudyActivity.class;
            else if (targetIndex == 4) targetClass = ProfileActivity.class;

            if (targetClass != null) {
                NavigationHelper.navigateWithTabAnimation(this, 2, targetIndex, targetClass);
                return true;
            }
            return false;
        });
    }
}
