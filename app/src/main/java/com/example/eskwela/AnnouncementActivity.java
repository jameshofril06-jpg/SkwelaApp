package com.example.eskwela;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView rvAnnouncements;
    private final List<String> announcementList = new ArrayList<>();
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

        adapter = new AnnouncementAdapter(announcementList);
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
                                announcementList.add("Welcome to ESKWELA! Stay tuned for official school announcements and updates.");
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
    }

    private void bindAnnouncements(com.google.firebase.firestore.QuerySnapshot result) {
        announcementList.clear();
        for (QueryDocumentSnapshot doc : result) {
            String message = doc.getString("message");
            if (message != null && !message.isEmpty()) {
                announcementList.add(message);
            }
        }
        if (announcementList.isEmpty()) {
            announcementList.add("Welcome to ESKWELA! Stay tuned for official school announcements and updates.");
        }
        adapter.notifyDataSetChanged();
    }

    private static class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.Holder> {
        private final List<String> items;

        AnnouncementAdapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.text.setText(items.get(position));
            holder.text.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary, null));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView text;

            Holder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
