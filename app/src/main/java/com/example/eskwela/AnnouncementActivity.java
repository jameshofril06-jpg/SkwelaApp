package com.example.eskwela;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView rvAnnouncements;
    private List<String> announcementList = new ArrayList<>();

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

        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        loadAnnouncements();
    }

    private void loadAnnouncements() {
        db.collection("announcements")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        announcementList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String message = doc.getString("message");
                            if (message != null) {
                                announcementList.add(message);
                            }
                        }
                        if (announcementList.isEmpty()) {
                            announcementList.add("Welcome to ESKWELA! Stay tuned for official school announcements and updates.");
                        }
                        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, announcementList);
                        rvAnnouncements.setAdapter(new RecyclerViewAdapterWrapper(adapter));
                    }
                });
    }

    // Simple adapter wrapper for ArrayAdapter in RecyclerView
    private static class RecyclerViewAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final android.widget.ArrayAdapter<String> arrayAdapter;

        public RecyclerViewAdapterWrapper(android.widget.ArrayAdapter<String> arrayAdapter) {
            this.arrayAdapter = arrayAdapter;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = arrayAdapter.getView(0, null, parent);
            return new RecyclerView.ViewHolder(view) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // bound via getView
        }

        @Override
        public int getItemCount() {
            return arrayAdapter.getCount();
        }
    }
}
