package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvDate, tvNextClassLabel, tvNextClassName, tvNextClassDetails, tvNoClasses;
    private View cardNextClass, quickActions, cardNoTasks;
    private RecyclerView rvDashboardClasses, rvDashboardTasks;

    private DashboardClassAdapter classAdapter;
    private DashboardTaskAdapter taskAdapter;

    private List<ScheduleCardAdapter.ScheduleItem> todayClasses = new ArrayList<>();
    private List<TaskAdapter.TaskItem> dueTasks = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String todayDateString;
    private boolean initialResumeSkipped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Date today = new Date();
        SimpleDateFormat displayDate = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        SimpleDateFormat dbDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        todayDateString = dbDate.format(today);

        // Bind Views
        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate);
        cardNextClass = findViewById(R.id.cardNextClass);
        tvNoClasses = findViewById(R.id.tvNoClasses);
        cardNoTasks = findViewById(R.id.cardNoTasks);

        tvNextClassLabel = findViewById(R.id.tvNextClassLabel);
        tvNextClassName = findViewById(R.id.tvNextClassName);
        tvNextClassDetails = findViewById(R.id.tvNextClassDetails);

        rvDashboardClasses = findViewById(R.id.rvDashboardClasses);
        rvDashboardTasks = findViewById(R.id.rvDashboardTasks);

        quickActions = findViewById(R.id.quickActions);

        tvDate.setText(displayDate.format(today));

        // Setup RecyclerViews
        rvDashboardClasses.setLayoutManager(new LinearLayoutManager(this));
        classAdapter = new DashboardClassAdapter(todayClasses);
        rvDashboardClasses.setAdapter(classAdapter);

        rvDashboardTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new DashboardTaskAdapter(dueTasks);
        rvDashboardTasks.setAdapter(taskAdapter);

        setupEntranceAnimations();
        loadData();

        // --- SETUP CLICK LISTENERS ---

        // Profile Icon Navigation
        findViewById(R.id.ivProfileIcon).setOnClickListener(v -> {
            NavigationHelper.navigateWithTabAnimation(DashboardActivity.this, 0, 4, ProfileActivity.class);
        });

        // See All Classes
        findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            NavigationHelper.navigateWithTabAnimation(DashboardActivity.this, 0, 1, MyScheduleActivity.class);
        });

        // View All Tasks
        findViewById(R.id.tvViewTasks).setOnClickListener(v -> {
            NavigationHelper.navigateWithTabAnimation(DashboardActivity.this, 0, 2, TasksActivity.class);
        });

        // Quick Actions
        CardView btnQuickAddClass = findViewById(R.id.btnQuickAddClass);
        if (btnQuickAddClass != null) {
            btnQuickAddClass.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, AddClassActivity.class);
                intent.putExtra("SELECTED_DATE", todayDateString);
                startActivity(intent);
            });
        }

        CardView btnQuickNewTask = findViewById(R.id.btnQuickNewTask);
        if (btnQuickNewTask != null) {
            btnQuickNewTask.setOnClickListener(v -> {
                NavigationHelper.navigateWithTabAnimation(DashboardActivity.this, 0, 2, TasksActivity.class);
            });
        }

        CardView btnQuickStartFocus = findViewById(R.id.btnQuickStartFocus);
        if (btnQuickStartFocus != null) {
            btnQuickStartFocus.setOnClickListener(v -> {
                NavigationHelper.navigateWithTabAnimation(DashboardActivity.this, 0, 3, StudyActivity.class);
            });
        }



        setupBottomNavigation();
    }

    private void loadData() {
        if (mAuth.getCurrentUser() == null) {
            AccountGuard.goLogin(this);
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        // 1. Fetch Greeting Name & Calculate Time of Day
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isFinishing()) return;
                    if (AccountGuard.blockIfRestricted(DashboardActivity.this, mAuth, documentSnapshot)) {
                        return;
                    }
                    String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : null;
                    if (AccountGuard.redirectIfAdmin(DashboardActivity.this, email, documentSnapshot.getString("role"))) {
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            String firstName = fullName.split(" ")[0];
                            setGreetingBasedOnTime(firstName);
                        }
                    }
                });

        // 2. Fetch Schedule
        db.collection("schedules")
                .whereEqualTo("userId", userId)
                .whereEqualTo("dateString", todayDateString)
                .get()
                .addOnCompleteListener(task -> {
                    if (isFinishing()) return;
                    if (task.isSuccessful()) {
                        todayClasses.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ScheduleCardAdapter.ScheduleItem item = document.toObject(ScheduleCardAdapter.ScheduleItem.class);
                            todayClasses.add(item);
                        }

                        sortClassesByTime(todayClasses);
                        classAdapter.notifyDataSetChanged();

                        if (todayClasses.isEmpty()) {
                            tvNoClasses.setVisibility(View.VISIBLE);
                            rvDashboardClasses.setVisibility(View.GONE);
                            cardNextClass.setVisibility(View.GONE);
                        } else {
                            tvNoClasses.setVisibility(View.GONE);
                            rvDashboardClasses.setVisibility(View.VISIBLE);
                            updateNextClassCard();
                        }
                    }
                });

        // 4. Fetch Latest Announcement for Banner
        db.collection("announcements")
                .orderBy("postedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing()) return;
                    View cardAnnounce = findViewById(R.id.cardDashboardAnnouncement);
                    TextView tvAnnounceText = findViewById(R.id.tvAnnouncementBannerText);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        String msg = queryDocumentSnapshots.getDocuments().get(0).getString("message");
                        if (msg != null && !msg.isEmpty()) {
                            if (cardAnnounce != null) cardAnnounce.setVisibility(View.VISIBLE);
                            if (tvAnnounceText != null) tvAnnounceText.setText(msg);
                            if (cardAnnounce != null) {
                                cardAnnounce.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AnnouncementActivity.class)));
                            }
                            View btnAnnounceBanner = findViewById(R.id.btnAnnouncementBanner);
                            if (btnAnnounceBanner != null) {
                                btnAnnounceBanner.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AnnouncementActivity.class)));
                            }
                        }
                    } else {
                        if (cardAnnounce != null) cardAnnounce.setVisibility(View.GONE);
                    }
                });

        // 3. Fetch Due Tasks (Not completed)
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("completed", false)
                .limit(3)
                .get()
                .addOnCompleteListener(task -> {
                    if (isFinishing()) return;
                    if (task.isSuccessful()) {
                        dueTasks.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TaskAdapter.TaskItem item = document.toObject(TaskAdapter.TaskItem.class);
                            item.id = document.getId();
                            dueTasks.add(item);
                        }

                        taskAdapter.notifyDataSetChanged();

                        if (dueTasks.isEmpty()) {
                            cardNoTasks.setVisibility(View.VISIBLE);
                            rvDashboardTasks.setVisibility(View.GONE);

                            // ANIMATION FOR EMPTY STATE
                            // Slide up and fade in
                            cardNoTasks.setAlpha(0f);
                            cardNoTasks.setTranslationY(30f);
                            cardNoTasks.animate()
                                    .alpha(1f)
                                    .translationY(0f)
                                    .setDuration(600)
                                    .setStartDelay(200)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start();
                        } else {
                            cardNoTasks.setVisibility(View.GONE);
                            rvDashboardTasks.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void setGreetingBasedOnTime(String firstName) {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String greeting = "";
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Good morning, " + firstName + "! ☀️";
        } else if (timeOfDay >= 12 && timeOfDay < 17) {
            greeting = "Good afternoon, " + firstName + "! 🌤️";
        } else if (timeOfDay >= 17 && timeOfDay < 21) {
            greeting = "Good evening, " + firstName + "! 🌙";
        } else {
            greeting = "Good night, " + firstName + "! 🌌";
        }

        tvGreeting.setText(greeting);
    }

    private void setupEntranceAnimations() {
        animateView(findViewById(R.id.tvGreeting), 100);
        animateView(findViewById(R.id.tvDate), 150);
        animateView(findViewById(R.id.ivProfileIcon), 200);
        animateView(cardNextClass, 300);
        animateView(findViewById(R.id.tvTodayHeader), 400);
        animateView(rvDashboardClasses, 450);
        animateView(findViewById(R.id.tvDueSoonHeader), 550);
        animateView(rvDashboardTasks, 600);
        animateView(quickActions, 700);
    }

    private void animateView(View view, long delayMs) {
        if (view == null) return;
        if (view.getVisibility() == View.GONE && (view == cardNextClass || view == tvNoClasses || view == cardNoTasks)) return;

        view.setAlpha(0f);
        view.setTranslationY(80f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(delayMs)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!initialResumeSkipped) {
            initialResumeSkipped = true;
            return;
        }
        loadData();
    }

    private void sortClassesByTime(List<ScheduleCardAdapter.ScheduleItem> classes) {
        Collections.sort(classes, new Comparator<ScheduleCardAdapter.ScheduleItem>() {
            @Override
            public int compare(ScheduleCardAdapter.ScheduleItem o1, ScheduleCardAdapter.ScheduleItem o2) {
                long time1 = parseTimeToMillis(o1.startTime);
                long time2 = parseTimeToMillis(o2.startTime);
                return Long.compare(time1, time2);
            }
        });
    }

    private long parseTimeToMillis(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) return 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = sdf.parse(timeString.trim());
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                cal.set(Calendar.SECOND, 0);
                return cal.getTimeInMillis();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateNextClassCard() {
        if (todayClasses.isEmpty()) return;

        long currentTimeMillis = System.currentTimeMillis();
        ScheduleCardAdapter.ScheduleItem nextClass = null;

        for (ScheduleCardAdapter.ScheduleItem item : todayClasses) {
            long classStartTime = parseTimeToMillis(item.startTime);
            if (classStartTime > currentTimeMillis) {
                nextClass = item;
                break;
            }
        }

        if (cardNextClass.getVisibility() == View.GONE) {
            cardNextClass.setVisibility(View.VISIBLE);
            cardNextClass.setAlpha(0f);
            cardNextClass.setTranslationY(50f);
            cardNextClass.animate().alpha(1f).translationY(0f).setDuration(600).start();
        }

        if (nextClass != null) {
            tvNextClassLabel.setText("NEXT CLASS • " + nextClass.startTime);
            tvNextClassName.setText(nextClass.className);
            String room = nextClass.room != null ? nextClass.room : "";
            String instructor = nextClass.instructor != null ? nextClass.instructor : "";
            String details = (room + " • " + instructor).replaceAll("(^\\s•\\s)|(\\s•\\s$)", "").trim();
            tvNextClassDetails.setText(details.isEmpty() ? "See schedule for details" : details);
        } else {
            tvNextClassLabel.setText("ALL DONE");
            tvNextClassName.setText("Classes finished");
            tvNextClassDetails.setText("Great job today! Time to rest.");
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int targetIndex = NavigationHelper.getTabIndex(item.getItemId());
            Class<?> targetClass = null;
            if (targetIndex == 0) return true;
            if (targetIndex == 1) targetClass = MyScheduleActivity.class;
            else if (targetIndex == 2) targetClass = TasksActivity.class;
            else if (targetIndex == 3) targetClass = StudyActivity.class;
            else if (targetIndex == 4) targetClass = ProfileActivity.class;

            if (targetClass != null) {
                NavigationHelper.navigateWithTabAnimation(this, 0, targetIndex, targetClass);
                return true;
            }
            return false;
        });
    }
}