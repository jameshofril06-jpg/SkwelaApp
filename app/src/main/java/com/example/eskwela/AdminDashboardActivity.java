package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView rvAdminAnnouncements;
    private TextView tvEmptyAnnouncements;
    private final List<AnnouncementActivity.AnnouncementItem> announcementList = new ArrayList<>();
    private AdminAnnouncementAdapter announcementAdapter;

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

        rvAdminAnnouncements = findViewById(R.id.rvAdminAnnouncements);
        tvEmptyAnnouncements = findViewById(R.id.tvEmptyAnnouncements);
        ImageView ivAdminProfile = findViewById(R.id.ivAdminProfile);

        if (ivAdminProfile != null) {
            ivAdminProfile.setOnClickListener(v -> {
                startActivity(new Intent(AdminDashboardActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
        }

        announcementAdapter = new AdminAnnouncementAdapter(announcementList, this::showAnnouncementDetailDialog);
        rvAdminAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        rvAdminAnnouncements.setAdapter(announcementAdapter);

        View btnPostAnn = findViewById(R.id.btnAdminPostAnnouncement);
        if (btnPostAnn != null) {
            btnPostAnn.setOnClickListener(v -> showPostAnnouncementDialog());
        }

        setupBottomNavigation();
        setupEntranceAnimations();
        loadAdminAnnouncements();
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
        View cardPost = findViewById(R.id.cardPostAnnouncement);
        View headerAnnounce = findViewById(R.id.tvAnnounceHeader);

        View[] viewsToAnimate = {cardPost, headerAnnounce, rvAdminAnnouncements};
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
                                .addOnSuccessListener(ref -> {
                                    Toast.makeText(this, "Announcement Posted to all students! 📢", Toast.LENGTH_SHORT).show();
                                    loadAdminAnnouncements();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadAdminAnnouncements() {
        db.collection("announcements")
                .orderBy("postedAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (isFinishing()) return;
                    if (task.isSuccessful()) {
                        announcementList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String id = doc.getId();
                            String message = doc.getString("message");
                            String author = doc.getString("author");
                            Long postedAt = doc.getLong("postedAt");

                            if (message != null && !message.isEmpty()) {
                                announcementList.add(new AnnouncementActivity.AnnouncementItem(
                                        id,
                                        message,
                                        author != null ? author : "ADMIN",
                                        postedAt != null ? postedAt : System.currentTimeMillis()
                                ));
                            }
                        }
                        announcementAdapter.notifyDataSetChanged();
                        if (tvEmptyAnnouncements != null) {
                            tvEmptyAnnouncements.setVisibility(announcementList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                });
    }

    private void showAnnouncementDetailDialog(AnnouncementActivity.AnnouncementItem item) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(item.postedAt));

        new AlertDialog.Builder(this)
                .setTitle("📢 Manage Announcement")
                .setMessage("Author: " + item.author + "\nDate: " + formattedDate + "\n\n" + item.message)
                .setPositiveButton("Share", (dialog, which) -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ESKWELA Announcement");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "📢 [ESKWELA Announcement]\n" + item.message + "\n\nPosted: " + formattedDate);
                    startActivity(Intent.createChooser(shareIntent, "Share Announcement via"));
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    db.collection("announcements").document(item.id).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Announcement deleted", Toast.LENGTH_SHORT).show();
                                loadAdminAnnouncements();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private interface OnAdminAnnouncementClickListener {
        void onAdminAnnouncementClick(AnnouncementActivity.AnnouncementItem item);
    }

    private static class AdminAnnouncementAdapter extends RecyclerView.Adapter<AdminAnnouncementAdapter.Holder> {
        private final List<AnnouncementActivity.AnnouncementItem> items;
        private final OnAdminAnnouncementClickListener listener;

        AdminAnnouncementAdapter(List<AnnouncementActivity.AnnouncementItem> items, OnAdminAnnouncementClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_announcement, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            AnnouncementActivity.AnnouncementItem item = items.get(position);
            holder.tvMessage.setText(item.message);
            holder.tvAuthor.setText(item.author != null && !item.author.isEmpty() ? "OFFICIAL " + item.author + " BROADCAST" : "OFFICIAL ADMIN BROADCAST");

            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(item.postedAt)));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAdminAnnouncementClick(item);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView tvMessage;
            final TextView tvAuthor;
            final TextView tvDate;

            Holder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvAnnounceMessage);
                tvAuthor = itemView.findViewById(R.id.tvAnnounceAuthor);
                tvDate = itemView.findViewById(R.id.tvAnnounceDate);
            }
        }
    }
}
