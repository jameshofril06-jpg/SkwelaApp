package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyScheduleActivity extends AppCompatActivity implements ScheduleCardAdapter.OnScheduleLongClickListener {

    private TextView tvSubtitle;
    private LinearLayout datesContainer;
    private RecyclerView rvDailySchedule;
    private TextView tvEmptyState;
    private ScheduleCardAdapter adapter;
    private List<ScheduleCardAdapter.ScheduleItem> dailyList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String selectedDateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_schedule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_schedule_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvSubtitle = findViewById(R.id.tvScheduleSubtitle);
        datesContainer = findViewById(R.id.datesContainer);
        rvDailySchedule = findViewById(R.id.rvDailySchedule);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvDailySchedule.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleCardAdapter(dailyList, this); // Pass 'this' for delete listener
        rvDailySchedule.setAdapter(adapter);

        generateRealTimeDates();
        setupAddButton();
        setupBottomNavigation();
    }

    private void generateRealTimeDates() {
        Calendar cal = Calendar.getInstance();

        // Set calendar to Monday of the current week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat numFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat subtitleFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());

        String startOfWeek = subtitleFormat.format(cal.getTime());

        // Default to today's date, but if today is Sunday, default to Monday
        Calendar todayCal = Calendar.getInstance();
        if (todayCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            todayCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        selectedDateString = dbFormat.format(todayCal.getTime());

        // Loop for 7 days (Mon - Sun)
        for (int i = 0; i < 7; i++) {
            final String currentDbDate = dbFormat.format(cal.getTime());
            String dayName = dayFormat.format(cal.getTime());
            String dayNum = numFormat.format(cal.getTime());

            LinearLayout dateLayout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(140, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dateLayout.setLayoutParams(params);
            dateLayout.setOrientation(LinearLayout.VERTICAL);
            dateLayout.setGravity(Gravity.CENTER);
            dateLayout.setPadding(0, 12, 0, 12);

            TextView tvDay = new TextView(this);
            tvDay.setText(dayName);
            tvDay.setTextSize(12);
            tvDay.setGravity(Gravity.CENTER);

            TextView tvNum = new TextView(this);
            tvNum.setText(dayNum);
            tvNum.setTextSize(16);
            tvNum.setTypeface(null, android.graphics.Typeface.BOLD);
            tvNum.setGravity(Gravity.CENTER);
            tvNum.setPadding(0, 4, 0, 0);

            dateLayout.addView(tvDay);
            dateLayout.addView(tvNum);

            // Style
            if (currentDbDate.equals(selectedDateString)) {
                styleSelectedDate(dateLayout, tvDay, tvNum);
            } else {
                styleUnselectedDate(dateLayout, tvDay, tvNum);
            }

            // Click Listener
            dateLayout.setOnClickListener(v -> {
                for (int j = 0; j < datesContainer.getChildCount(); j++) {
                    LinearLayout child = (LinearLayout) datesContainer.getChildAt(j);
                    styleUnselectedDate(child, (TextView) child.getChildAt(0), (TextView) child.getChildAt(1));
                }
                styleSelectedDate(dateLayout, tvDay, tvNum);
                selectedDateString = currentDbDate;
                fetchScheduleForDate(currentDbDate);
            });

            datesContainer.addView(dateLayout);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        cal.add(Calendar.DAY_OF_MONTH, -1);
        tvSubtitle.setText(startOfWeek + " – " + subtitleFormat.format(cal.getTime()));

        fetchScheduleForDate(selectedDateString);
    }

    private void styleSelectedDate(LinearLayout layout, TextView day, TextView num) {
        layout.setBackgroundResource(R.drawable.bg_date_selected);
        day.setTextColor(ContextCompat.getColor(this, R.color.white));
        num.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void styleUnselectedDate(LinearLayout layout, TextView day, TextView num) {
        layout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        day.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
        num.setTextColor(android.graphics.Color.parseColor("#0F172A"));
    }

    private void fetchScheduleForDate(String date) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("schedules")
                .whereEqualTo("userId", userId)
                .whereEqualTo("dateString", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dailyList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ScheduleCardAdapter.ScheduleItem item = document.toObject(ScheduleCardAdapter.ScheduleItem.class);
                            item.id = document.getId();
                            dailyList.add(item);
                        }

                        adapter.notifyDataSetChanged();
                        tvEmptyState.setVisibility(dailyList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    // Handle long press to delete
    @Override
    public void onScheduleLongClick(ScheduleCardAdapter.ScheduleItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete " + item.className + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("schedules").document(item.id).delete()
                            .addOnSuccessListener(aVoid -> {
                                dailyList.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, dailyList.size());
                                tvEmptyState.setVisibility(dailyList.isEmpty() ? View.VISIBLE : View.GONE);
                                Toast.makeText(this, "Class deleted", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupAddButton() {
        CardView btnAddSchedule = findViewById(R.id.btnAddSchedule);
        btnAddSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(MyScheduleActivity.this, AddClassActivity.class);
            intent.putExtra("SELECTED_DATE", selectedDateString);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchScheduleForDate(selectedDateString);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_schedule);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MyScheduleActivity.this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_schedule) {
                return true;
            } else if (itemId == R.id.nav_tasks) {
                startActivity(new Intent(MyScheduleActivity.this, TasksActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_study) {
                // FIXED: Now directs to StudyActivity
                startActivity(new Intent(MyScheduleActivity.this, StudyActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MyScheduleActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}