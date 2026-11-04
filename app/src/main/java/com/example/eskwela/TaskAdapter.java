package com.example.eskwela;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<TaskItem> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskCheck(TaskItem task, int position);
        void onTaskDelete(TaskItem task, int position);
    }

    public TaskAdapter(List<TaskItem> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskItem task = taskList.get(position);

        holder.tvName.setText(task.title != null ? task.title : "Task");
        holder.tvDate.setText(task.dateString != null ? "Due: " + task.dateString : "Due: No deadline");

        String course = task.courseName != null && !task.courseName.isEmpty() ? task.courseName : "General";
        holder.tvCourseTag.setText("Course: " + course);

        String priority = task.priority != null ? task.priority.toUpperCase() : "MEDIUM";
        holder.tvPriority.setText(priority);
        if ("HIGH".equals(priority)) {
            holder.tvPriority.setTextColor(0xFFEF4444); // Red
        } else if ("LOW".equals(priority)) {
            holder.tvPriority.setTextColor(0xFF16A34A); // Green
        } else {
            holder.tvPriority.setTextColor(0xFFF59E0B); // Amber
        }

        // Update UI based on completed status
        if (task.completed) {
            holder.ivStatus.setImageResource(R.drawable.ic_check_circle_green);
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvName.setTextColor(0xFF94A3B8);
            holder.itemView.setAlpha(0.7f);
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_task_circle);
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary, null));
            holder.itemView.setAlpha(1f);
        }

        holder.ivStatus.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onTaskCheck(taskList.get(adapterPosition), adapterPosition);
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onTaskDelete(taskList.get(adapterPosition), adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvPriority, tvCourseTag;
        ImageView ivStatus, ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTaskItemName);
            tvDate = itemView.findViewById(R.id.tvTaskItemDate);
            tvPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvCourseTag = itemView.findViewById(R.id.tvTaskCourseTag);
            ivStatus = itemView.findViewById(R.id.ivTaskStatus);
            ivDelete = itemView.findViewById(R.id.ivTaskDelete);
        }
    }

    public static class TaskItem {
        public String id;
        public String title;
        public String dateString;
        public String courseName;
        public String priority; // "HIGH", "MEDIUM", "LOW"
        public boolean completed;

        public TaskItem() {}
    }
}
