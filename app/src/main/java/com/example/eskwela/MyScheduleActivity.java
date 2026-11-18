package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyScheduleActivity extends AppCompatActivity implements ScheduleCardAdapter.OnScheduleLongClickListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvSubtitle, tvEmptyState;
    private LinearLayout datesContainer;
    private RecyclerView rvDailySchedule;
    private ScheduleCardAdapter adapter;

    private final List<ScheduleCardAdapter.ScheduleItem> dailyList = new ArrayList<>();
    private String selectedDateString; // Format: yyyy-MM-dd

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
        adapter = new ScheduleCardAdapter(dailyList, this);
        rvDailySchedule.setAdapter(adapter);

        View cardScanBanner = findViewById(R.id.cardScanStudyLoadBanner);
        if (cardScanBanner != null) {
            cardScanBanner.setOnClickListener(v -> startActivity(new Intent(MyScheduleActivity.this, ScheduleActivity.class)));
        }
        View btnScanTrigger = findViewById(R.id.btnScanStudyLoadTrigger);
        if (btnScanTrigger != null) {
            btnScanTrigger.setOnClickListener(v -> startActivity(new Intent(MyScheduleActivity.this, ScheduleActivity.class)));
        }

        setupViewToggles();
        generateRealTimeDates();
        setupAddButton();
        setupBottomNavigation();
    }

    private void setupViewToggles() {
        TextView tabDay = findViewById(R.id.tabScheduleDay);
        TextView tabWeek = findViewById(R.id.tabScheduleWeek);
        TextView tabMonth = findViewById(R.id.tabScheduleMonth);

        if (tabDay != null) {
            tabDay.setOnClickListener(v -> {
                tabDay.setBackgroundResource(R.drawable.bg_segment_active);
                tabDay.setTextColor(0xFF1D4ED8);
                tabDay.setTypeface(null, android.graphics.Typeface.BOLD);
                if (tabWeek != null) { tabWeek.setBackgroundColor(0); tabWeek.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); tabWeek.setTypeface(null, android.graphics.Typeface.NORMAL); }
                if (tabMonth != null) { tabMonth.setBackgroundColor(0); tabMonth.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); tabMonth.setTypeface(null, android.graphics.Typeface.NORMAL); }
                fetchScheduleForDate(selectedDateString);
            });
        }

        if (tabWeek != null) {
            tabWeek.setOnClickListener(v -> {
                tabWeek.setBackgroundResource(R.drawable.bg_segment_active);
                tabWeek.setTextColor(0xFF1D4ED8);
                tabWeek.setTypeface(null, android.graphics.Typeface.BOLD);
                if (tabDay != null) { tabDay.setBackgroundColor(0); tabDay.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); tabDay.setTypeface(null, android.graphics.Typeface.NORMAL); }
                if (tabMonth != null) { tabMonth.setBackgroundColor(0); tabMonth.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); tabMonth.setTypeface(null, android.graphics.Typeface.NORMAL); }
                fetchAllSchedulesForWeek();
            });
        }

        if (tabMonth != null) {
            tabMonth.setOnClickListener(v -> {
                tabMonth.setBackgroundResource(R.drawable.bg_segment_active);
                tabMonth.setTextColor(0xFF1D4ED8);
                tabMonth.setTypeface(null, android.graphics.Typeface.BOLD);
                if (tabDay != null) { tabDay.setBackgroundColor(0); tabDay.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); tabDay.setTypeface(null, android.graphics.Typeface.NORMAL); }
                if (tabWeek != null) { tabWeek.setBackgroundColor(0); tabWeek.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); tabWeek.setTypeface(null, android.graphics.Typeface.NORMAL); }
                fetchAllSchedulesForMonth();
            });
        }
    }

    private void fetchAllSchedulesForWeek() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("schedules")
                .whereEqualTo("userId", userId)
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
                        if (tvSubtitle != null) tvSubtitle.setText("All Week Classes (" + dailyList.size() + ")");
                    }
                });
    }

    private void fetchAllSchedulesForMonth() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("schedules")
                .whereEqualTo("userId", userId)
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
                        if (tvSubtitle != null) tvSubtitle.setText("All Month Classes (" + dailyList.size() + ")");
                    }
                });
    }

    private void generateRealTimeDates() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat numFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat subtitleFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());

        String startOfWeek = subtitleFormat.format(cal.getTime());

        Calendar todayCal = Calendar.getInstance();
        selectedDateString = dbFormat.format(todayCal.getTime());

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

            if (currentDbDate.equals(selectedDateString)) {
                styleSelectedDate(dateLayout, tvDay, tvNum);
            } else {
                styleUnselectedDate(dateLayout, tvDay, tvNum);
            }

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
        if (tvSubtitle != null) tvSubtitle.setText(startOfWeek + " – " + subtitleFormat.format(cal.getTime()));

        fetchScheduleForDate(selectedDateString);
    }

    private void styleSelectedDate(LinearLayout layout, TextView day, TextView num) {
        layout.setBackgroundResource(R.drawable.bg_date_selected);
        day.setTextColor(ContextCompat.getColor(this, R.color.white));
        num.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void styleUnselectedDate(LinearLayout layout, TextView day, TextView num) {
        layout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        day.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        num.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
    }

    private void fetchScheduleForDate(String date) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("schedules")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dailyList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ScheduleCardAdapter.ScheduleItem item = document.toObject(ScheduleCardAdapter.ScheduleItem.class);
                            item.id = document.getId();

                            // Strict date matching: ONLY show classes scheduled on this specific date
                            if (item.dateString != null && (item.dateString.equals(date) || item.dateString.startsWith(date))) {
                                dailyList.add(item);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        tvEmptyState.setVisibility(dailyList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    // Handle long press to delete with professional validation
    @Override
    public void onScheduleLongClick(ScheduleCardAdapter.ScheduleItem item, int position) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        TextView tvWarning = new TextView(this);
        tvWarning.setText("⚠️ WARNING: Deleting this class will remove it from your schedule.\n\nTo confirm deletion, please type the exact Class Code below:");
        tvWarning.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvWarning.setTextSize(14);

        final EditText etConfirmCode = new EditText(this);
        etConfirmCode.setHint("e.g. " + (item.classCode != null && !item.classCode.isEmpty() ? item.classCode : item.className));
        etConfirmCode.setPadding(32, 24, 32, 24);
        etConfirmCode.setBackgroundResource(R.drawable.bg_input_field);
        etConfirmCode.setSingleLine(true);

        layout.addView(tvWarning);
        layout.addView(etConfirmCode);

        final String expectedCode = item.classCode != null && !item.classCode.isEmpty() ? item.classCode.trim() : item.className.trim();

        new AlertDialog.Builder(this)
                .setTitle("Delete Scheduled Class")
                .setView(layout)
                .setPositiveButton("Delete Class", (dialog, which) -> {
                    String inputCode = etConfirmCode.getText().toString().trim();
                    if (inputCode.equalsIgnoreCase(expectedCode)) {
                        deleteScheduleItem(item, position);
                    } else {
                        Toast.makeText(this, "Class Code mismatch. Deletion cancelled.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteScheduleItem(ScheduleCardAdapter.ScheduleItem item, int position) {
        if (item.id != null && !item.id.isEmpty()) {
            db.collection("schedules").document(item.id).delete()
                    .addOnSuccessListener(aVoid -> {
                        dailyList.remove(position);
                        adapter.notifyItemRemoved(position);
                        tvEmptyState.setVisibility(dailyList.isEmpty() ? View.VISIBLE : View.GONE);
                        Toast.makeText(this, "Class deleted successfully 🗑️", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void setupAddButton() {
        View btnAddSchedule = findViewById(R.id.btnAddSchedule);
        if (btnAddSchedule != null) {
            btnAddSchedule.setOnClickListener(v -> {
                Intent intent = new Intent(MyScheduleActivity.this, AddClassActivity.class);
                intent.putExtra("SELECTED_DATE", selectedDateString);
                startActivity(intent);
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_schedule);

        bottomNav.setOnItemSelectedListener(item -> {
            int targetIndex = NavigationHelper.getTabIndex(item.getItemId());
            Class<?> targetClass = null;
            if (targetIndex == 1) return true;
            if (targetIndex == 0) targetClass = DashboardActivity.class;
            else if (targetIndex == 2) targetClass = TasksActivity.class;
            else if (targetIndex == 3) targetClass = StudyActivity.class;
            else if (targetIndex == 4) targetClass = ProfileActivity.class;

            if (targetClass != null) {
                NavigationHelper.navigateWithTabAnimation(this, 1, targetIndex, targetClass);
                return true;
            }
            return false;
        });
    }
}
