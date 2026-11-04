package com.example.eskwela;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(UserItem user);
    }

    private List<UserItem> userList;
    private OnUserClickListener listener;

    public static class UserItem {
        public String id;
        public String fullName;
        public String email;
        public String studentId;
        public String school;
        public String course;
        public String year;
        public String status; // "active", "suspended", "deactivated", "archived"
        public String suspensionDuration; // "24 Hours", "7 Days", "30 Days", "Indefinite"
        public Long suspendedAt; // Milliseconds timestamp

        public UserItem() {}
    }

    public AdminUserAdapter(List<UserItem> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserItem user = userList.get(position);
        holder.tvUserName.setText(user.fullName != null && !user.fullName.isEmpty() ? user.fullName : "Unnamed User");
        holder.tvUserEmail.setText(user.email != null ? user.email : "No email");
        holder.tvUserStudentId.setText(user.studentId != null ? "ID: " + user.studentId : "ID: N/A");

        String statusStr = user.status != null ? user.status.toUpperCase() : "ACTIVE";
        if ("SUSPENDED".equals(statusStr)) {
            String remaining = calculateRemainingSuspensionTime(user.suspensionDuration, user.suspendedAt);
            holder.tvUserStatus.setText("SUSPENDED (" + remaining + ")");
            holder.tvUserStatus.setTextColor(0xFFEF4444); // Red
        } else if ("DEACTIVATED".equals(statusStr)) {
            holder.tvUserStatus.setText(statusStr);
            holder.tvUserStatus.setTextColor(0xFFEF4444); // Red
        } else if ("ARCHIVED".equals(statusStr)) {
            holder.tvUserStatus.setText(statusStr);
            holder.tvUserStatus.setTextColor(0xFF64748B); // Slate
        } else {
            holder.tvUserStatus.setText(statusStr);
            holder.tvUserStatus.setTextColor(0xFF16A34A); // Green
        }

        String schoolCourse = "";
        if (user.school != null && !user.school.isEmpty()) schoolCourse += user.school;
        if (user.course != null && !user.course.isEmpty()) {
            if (!schoolCourse.isEmpty()) schoolCourse += " · ";
            schoolCourse += user.course;
        }
        if (schoolCourse.isEmpty()) schoolCourse = "No school/course info";
        holder.tvUserSchoolCourse.setText(schoolCourse);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    public static String calculateRemainingSuspensionTime(String duration, Long suspendedAt) {
        if (suspendedAt == null || suspendedAt <= 0) {
            return duration != null ? duration : "Suspended";
        }
        if ("Indefinite".equalsIgnoreCase(duration)) {
            return "Indefinite";
        }

        long durationMs = 24 * 60 * 60 * 1000L; // default 24h
        if ("7 Days".equalsIgnoreCase(duration)) {
            durationMs = 7 * 24 * 60 * 60 * 1000L;
        } else if ("30 Days".equalsIgnoreCase(duration)) {
            durationMs = 30 * 24 * 60 * 60 * 1000L;
        }

        long remainingMs = (suspendedAt + durationMs) - System.currentTimeMillis();
        if (remainingMs <= 0) {
            return "Expired (Pending Activation)";
        }

        long hours = remainingMs / (1000 * 60 * 60);
        long minutes = (remainingMs / (1000 * 60)) % 60;
        long days = hours / 24;

        if (days > 0) {
            long remHours = hours % 24;
            return days + "d " + remHours + "h remaining";
        } else {
            return hours + "h " + minutes + "m remaining";
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserStudentId, tvUserSchoolCourse, tvUserStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserStudentId = itemView.findViewById(R.id.tvUserStudentId);
            tvUserSchoolCourse = itemView.findViewById(R.id.tvUserSchoolCourse);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
        }
    }
}
