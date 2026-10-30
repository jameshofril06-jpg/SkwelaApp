package com.example.eskwela;

import android.os.Bundle;
import android.widget.Button;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private AdminUserAdapter userAdapter;
    private List<AdminUserAdapter.UserItem> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalSchedules = findViewById(R.id.tvTotalSchedules);
        rvAdminUsers = findViewById(R.id.rvAdminUsers);
        ImageView ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        rvAdminUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new AdminUserAdapter(userList, this::showUserActionDialog);
        rvAdminUsers.setAdapter(userAdapter);

        android.view.View btnPostAnn = findViewById(R.id.btnAdminPostAnnouncement);
        if (btnPostAnn != null) {
            btnPostAnn.setOnClickListener(v -> showPostAnnouncementDialog());
        }

        loadAdminData();
    }

    private void showPostAnnouncementDialog() {
        android.widget.EditText etMessage = new android.widget.EditText(this);
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

    private void loadAdminData() {
        // 1. Fetch Users
        db.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            AdminUserAdapter.UserItem item = doc.toObject(AdminUserAdapter.UserItem.class);
                            item.id = doc.getId();
                            userList.add(item);
                        }
                        userAdapter.notifyDataSetChanged();
                        tvTotalUsers.setText(String.valueOf(userList.size()));
                    } else {
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });

        // 2. Fetch Total Schedules count in system
        db.collection("schedules").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        tvTotalSchedules.setText(String.valueOf(count));
                    }
                });
    }

    private void showUserActionDialog(AdminUserAdapter.UserItem user) {
        String name = user.fullName != null ? user.fullName : "User";
        String currentStatus = user.status != null ? user.status : "active";

        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        android.view.View paneView = getLayoutInflater().inflate(R.layout.layout_admin_user_pane, null);
        bottomSheet.setContentView(paneView);

        TextView tvName = paneView.findViewById(R.id.tvPaneUserName);
        TextView tvDetails = paneView.findViewById(R.id.tvPaneUserDetails);
        TextView tvStatus = paneView.findViewById(R.id.tvPaneUserStatus);
        Button btnSuspend = paneView.findViewById(R.id.btnPaneSuspend);
        Button btnToggleActive = paneView.findViewById(R.id.btnPaneToggleActive);
        Button btnArchive = paneView.findViewById(R.id.btnPaneArchive);

        tvName.setText(name);
        tvDetails.setText((user.email != null ? user.email : "No Email") + " • " + (user.studentId != null ? "ID: " + user.studentId : "N/A"));
        tvStatus.setText("STATUS: " + currentStatus.toUpperCase());

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
                                loadAdminData();
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
                    loadAdminData();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
