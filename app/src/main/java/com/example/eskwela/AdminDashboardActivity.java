package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvTotalUsers, tvTotalSchedules;
    private RecyclerView rvAdminUsers;
    private EditText etSearchUsers;
    private AdminUserAdapter userAdapter;
    private final List<AdminUserAdapter.UserItem> userList = new ArrayList<>();
    private final List<AdminUserAdapter.UserItem> allUserList = new ArrayList<>();
    private String currentStatusFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            AccountGuard.goLogin(this);
            return;
        }

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalSchedules = findViewById(R.id.tvTotalSchedules);
        rvAdminUsers = findViewById(R.id.rvAdminUsers);
        etSearchUsers = findViewById(R.id.etSearchUsers);
        ImageView ivAdminProfile = findViewById(R.id.ivAdminProfile);

        if (ivAdminProfile != null) {
            ivAdminProfile.setOnClickListener(v -> {
                startActivity(new Intent(AdminDashboardActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
        }

        rvAdminUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new AdminUserAdapter(userList, this::showUserProfileAndModerationDialog);
        rvAdminUsers.setAdapter(userAdapter);

        if (etSearchUsers != null) {
            etSearchUsers.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterUserList(s.toString().trim(), currentStatusFilter);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        View btnPostAnn = findViewById(R.id.btnAdminPostAnnouncement);
        if (btnPostAnn != null) {
            btnPostAnn.setOnClickListener(v -> showPostAnnouncementDialog());
        }

        setupFilterChips();
        setupBottomNavigation();
        setupEntranceAnimations();
        loadAdminDataRealtime();
    }

    private void setupFilterChips() {
        TextView chipAll = findViewById(R.id.chipFilterAll);
        TextView chipActive = findViewById(R.id.chipFilterActive);
        TextView chipSuspended = findViewById(R.id.chipFilterSuspended);
        TextView chipDeactivated = findViewById(R.id.chipFilterDeactivated);
        TextView chipArchived = findViewById(R.id.chipFilterArchived);

        View.OnClickListener filterListener = v -> {
            String tag = v.getTag() != null ? v.getTag().toString() : "ALL";
            currentStatusFilter = tag;

            if (chipAll != null) highlightChip(chipAll, tag.equals("ALL"));
            if (chipActive != null) highlightChip(chipActive, tag.equals("ACTIVE"));
            if (chipSuspended != null) highlightChip(chipSuspended, tag.equals("SUSPENDED"));
            if (chipDeactivated != null) highlightChip(chipDeactivated, tag.equals("DEACTIVATED"));
            if (chipArchived != null) highlightChip(chipArchived, tag.equals("ARCHIVED"));

            filterUserList(etSearchUsers != null ? etSearchUsers.getText().toString().trim() : "", currentStatusFilter);
        };

        if (chipAll != null) { chipAll.setTag("ALL"); chipAll.setOnClickListener(filterListener); }
        if (chipActive != null) { chipActive.setTag("ACTIVE"); chipActive.setOnClickListener(filterListener); }
        if (chipSuspended != null) { chipSuspended.setTag("SUSPENDED"); chipSuspended.setOnClickListener(filterListener); }
        if (chipDeactivated != null) { chipDeactivated.setTag("DEACTIVATED"); chipDeactivated.setOnClickListener(filterListener); }
        if (chipArchived != null) { chipArchived.setTag("ARCHIVED"); chipArchived.setOnClickListener(filterListener); }
    }

    private void highlightChip(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_segment_active);
            chip.setTextColor(0xFF1D4ED8);
        } else {
            chip.setBackgroundResource(R.drawable.bg_segment_inactive);
            chip.setTextColor(0xFF64748B);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) return true;
                if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(AdminDashboardActivity.this, ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
                return false;
            });
        }
    }

    private void setupEntranceAnimations() {
        View cardStats = findViewById(R.id.cardStats);
        View cardPost = findViewById(R.id.cardPostAnnouncement);
        View headerUsers = findViewById(R.id.tvUsersHeader);

        View[] viewsToAnimate = {cardStats, cardPost, headerUsers, etSearchUsers, rvAdminUsers};
        long delay = 100;
        for (View v : viewsToAnimate) {
            if (v != null) {
                v.setAlpha(0f);
                v.setTranslationY(40f);
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setStartDelay(delay)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                delay += 80;
            }
        }
    }

    private void filterUserList(String query, String statusFilter) {
        userList.clear();
        String lowerQuery = query.toLowerCase();

        for (AdminUserAdapter.UserItem item : allUserList) {
            String itemStatus = item.status != null ? item.status.toUpperCase() : "ACTIVE";
            boolean matchesStatus = statusFilter.equals("ALL") || itemStatus.equals(statusFilter);

            boolean matchName = item.fullName != null && item.fullName.toLowerCase().contains(lowerQuery);
            boolean matchEmail = item.email != null && item.email.toLowerCase().contains(lowerQuery);
            boolean matchId = item.studentId != null && item.studentId.toLowerCase().contains(lowerQuery);
            boolean matchesQuery = lowerQuery.isEmpty() || matchName || matchEmail || matchId;

            if (matchesStatus && matchesQuery) {
                userList.add(item);
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    private void showPostAnnouncementDialog() {
        EditText etMessage = new EditText(this);
        etMessage.setHint("Type official announcement message...");
        etMessage.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle("Broadcast Official Announcement")
                .setView(etMessage)
                .setPositiveButton("Post Announcement", (dialog, which) -> {
                    String msg = etMessage.getText().toString().trim();
                    if (!msg.isEmpty()) {
                        Map<String, Object> announcement = new HashMap<>();
                        announcement.put("message", msg);
                        announcement.put("postedAt", System.currentTimeMillis());
                        announcement.put("author", "ADMIN");

                        db.collection("announcements").add(announcement)
                                .addOnSuccessListener(ref -> Toast.makeText(this, "Announcement Posted to all students! 📢", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadAdminDataRealtime() {
        db.collection("users").addSnapshotListener((value, error) -> {
            if (isFinishing()) return;
            allUserList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    AdminUserAdapter.UserItem item = doc.toObject(AdminUserAdapter.UserItem.class);
                    item.id = doc.getId();
                    allUserList.add(item);
                }
            }

            // Fallback Mock Students if Firestore returns empty
            if (allUserList.isEmpty()) {
                AdminUserAdapter.UserItem m1 = new AdminUserAdapter.UserItem();
                m1.id = "student-uid-101";
                m1.fullName = "Juan Dela Cruz";
                m1.email = "juandelacruz@student.edu.ph";
                m1.studentId = "STU-4921";
                m1.school = "University of the Philippines";
                m1.course = "BS Information Technology";
                m1.year = "4th Year";
                m1.section = "IT41C1";
                m1.status = "active";
                allUserList.add(m1);

                AdminUserAdapter.UserItem m2 = new AdminUserAdapter.UserItem();
                m2.id = "student-uid-102";
                m2.fullName = "Maria Santos";
                m2.email = "mariasantos@student.edu.ph";
                m2.studentId = "STU-8823";
                m2.school = "University of the Philippines";
                m2.course = "BS Computer Science";
                m2.year = "3rd Year";
                m2.section = "CS3A";
                m2.status = "suspended";
                m2.suspensionDuration = "7 Days";
                m2.suspendedAt = System.currentTimeMillis() - 86400000L;
                allUserList.add(m2);

                AdminUserAdapter.UserItem m3 = new AdminUserAdapter.UserItem();
                m3.id = "student-uid-103";
                m3.fullName = "Pedro Penduko";
                m3.email = "pedropenduko@student.edu.ph";
                m3.studentId = "STU-1102";
                m3.school = "University of the Philippines";
                m3.course = "BS Information Systems";
                m3.year = "2nd Year";
                m3.section = "IS2B";
                m3.status = "deactivated";
                allUserList.add(m3);
            }

            if (tvTotalUsers != null) tvTotalUsers.setText(String.valueOf(allUserList.size()));
            filterUserList(etSearchUsers != null ? etSearchUsers.getText().toString().trim() : "", currentStatusFilter);
        });

        db.collection("schedules").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && tvTotalSchedules != null) {
                        tvTotalSchedules.setText(String.valueOf(task.getResult().size()));
                    }
                });
    }

    private void showUserProfileAndModerationDialog(AdminUserAdapter.UserItem user) {
        String name = user.fullName != null ? user.fullName : "User";
        String currentStatus = user.status != null ? user.status : "active";

        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View paneView = getLayoutInflater().inflate(R.layout.layout_admin_user_pane, null);
        bottomSheet.setContentView(paneView);

        TextView tvName = paneView.findViewById(R.id.tvPaneUserName);
        TextView tvDetails = paneView.findViewById(R.id.tvPaneUserDetails);
        TextView tvStatus = paneView.findViewById(R.id.tvPaneUserStatus);
        Button btnSuspend = paneView.findViewById(R.id.btnPaneSuspend);
        Button btnToggleActive = paneView.findViewById(R.id.btnPaneToggleActive);
        Button btnArchive = paneView.findViewById(R.id.btnPaneArchive);

        tvName.setText(name);
        String details = (user.email != null ? user.email : "No Email") + "\n" +
                (user.school != null ? user.school : "No School") + " • " + (user.course != null ? user.course : "") + "\n" +
                (user.studentId != null ? "Student ID: " + user.studentId : "ID: N/A") + " • " + (user.year != null ? user.year : "") + " " + (user.section != null ? user.section : "");
        tvDetails.setText(details);

        if ("suspended".equalsIgnoreCase(currentStatus)) {
            String remainingTime = AdminUserAdapter.calculateRemainingSuspensionTime(user.suspensionDuration, user.suspendedAt);
            tvStatus.setText("STATUS: SUSPENDED\n⏳ Remaining: " + remainingTime);
            tvStatus.setTextColor(0xFFEF4444);
        } else {
            tvStatus.setText("STATUS: " + currentStatus.toUpperCase());
        }

        btnToggleActive.setText("active".equalsIgnoreCase(currentStatus) ? "Deactivate Account" : "Activate Account");

        btnSuspend.setOnClickListener(v -> {
            bottomSheet.dismiss();
            showSuspendDurationDialog(user);
        });

        btnToggleActive.setOnClickListener(v -> {
            bottomSheet.dismiss();
            String newStatus = "active".equalsIgnoreCase(currentStatus) ? "deactivated" : "active";
            updateUserStatus(user, newStatus, "Account status changed to " + newStatus);
        });

        btnArchive.setOnClickListener(v -> {
            bottomSheet.dismiss();
            updateUserStatus(user, "archived", "Account archived");
        });

        bottomSheet.show();
    }

    private void showSuspendDurationDialog(AdminUserAdapter.UserItem user) {
        CharSequence[] durations = new CharSequence[] { "24 Hours", "7 Days", "30 Days", "Indefinite" };
        new AlertDialog.Builder(this)
                .setTitle("Select Suspension Duration")
                .setItems(durations, (dialog, which) -> {
                    String duration = durations[which].toString();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "suspended");
                    updates.put("suspensionDuration", duration);
                    updates.put("suspendedAt", System.currentTimeMillis());

                    db.collection("users").document(user.id).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User suspended for " + duration, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .show();
    }

    private void updateUserStatus(AdminUserAdapter.UserItem user, String newStatus, String successMsg) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        db.collection("users").document(user.id).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
