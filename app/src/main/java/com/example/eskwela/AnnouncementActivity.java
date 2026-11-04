package com.example.eskwela;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnnouncementActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView rvAnnouncements;
    private final List<AnnouncementItem> announcementList = new ArrayList<>();
    private AnnouncementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_announcement);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.announcement_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        rvAnnouncements = findViewById(R.id.rvAnnouncements);
        ImageView ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        adapter = new AnnouncementAdapter(announcementList, this::showAnnouncementDetailDialog);
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        rvAnnouncements.setAdapter(adapter);
        loadAnnouncements();
    }

    private void loadAnnouncements() {
        db.collection("announcements")
                .orderBy("postedAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (isFinishing()) return;
                    if (task.isSuccessful()) {
                        bindAnnouncements(task.getResult());
                    } else {
                        db.collection("announcements").get().addOnCompleteListener(fallback -> {
                            if (isFinishing()) return;
                            if (fallback.isSuccessful()) {
                                bindAnnouncements(fallback.getResult());
                            } else {
                                announcementList.clear();
                                announcementList.add(new AnnouncementItem("welcome-id", "Welcome to ESKWELA! Stay tuned for official school announcements and updates.", "ADMIN", System.currentTimeMillis()));
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
    }

    private void bindAnnouncements(com.google.firebase.firestore.QuerySnapshot result) {
        announcementList.clear();
        for (QueryDocumentSnapshot doc : result) {
            String id = doc.getId();
            String message = doc.getString("message");
            String author = doc.getString("author");
            Long postedAt = doc.getLong("postedAt");

            if (message != null && !message.isEmpty()) {
                announcementList.add(new AnnouncementItem(
                        id,
                        message,
                        author != null ? author : "ADMIN",
                        postedAt != null ? postedAt : System.currentTimeMillis()
                ));
            }
        }
        if (announcementList.isEmpty()) {
            announcementList.add(new AnnouncementItem("welcome-id", "Welcome to ESKWELA! Stay tuned for official school announcements and updates.", "ADMIN", System.currentTimeMillis()));
        }
        adapter.notifyDataSetChanged();
    }

    private void showAnnouncementDetailDialog(AnnouncementItem item) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(item.postedAt));

        new AlertDialog.Builder(this)
                .setTitle("📢 Official Announcement Log")
                .setMessage("Author: " + item.author + "\nDate: " + formattedDate + "\n\n" + item.message)
                .setPositiveButton("Share", (dialog, which) -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ESKWELA Announcement");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "📢 [ESKWELA Announcement]\n" + item.message + "\n\nPosted: " + formattedDate);
                    startActivity(Intent.createChooser(shareIntent, "Share Announcement via"));
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static class AnnouncementItem {
        public String id;
        public String message;
        public String author;
        public long postedAt;

        public AnnouncementItem(String id, String message, String author, long postedAt) {
            this.id = id;
            this.message = message;
            this.author = author;
            this.postedAt = postedAt;
        }
    }

    private interface OnAnnouncementClickListener {
        void onAnnouncementClick(AnnouncementItem item);
    }

    private static class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.Holder> {
        private final List<AnnouncementItem> items;
        private final OnAnnouncementClickListener listener;

        AnnouncementAdapter(List<AnnouncementItem> items, OnAnnouncementClickListener listener) {
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
            AnnouncementItem item = items.get(position);
            holder.tvMessage.setText(item.message);
            holder.tvAuthor.setText(item.author != null && !item.author.isEmpty() ? "OFFICIAL " + item.author + " BROADCAST" : "OFFICIAL ADMIN BROADCAST");

            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(item.postedAt)));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAnnouncementClick(item);
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
