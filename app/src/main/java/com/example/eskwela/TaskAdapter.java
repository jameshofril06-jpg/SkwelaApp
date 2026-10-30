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

        holder.tvName.setText(task.title);
        holder.tvDate.setText(task.dateString);

        // Update UI based on the boolean value
        if (task.completed) {
            holder.ivStatus.setImageResource(R.drawable.ic_check_circle_green);
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvName.setTextColor(0xFF94A3B8);
            holder.itemView.setAlpha(0.7f);
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_task_circle);
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvName.setTextColor(0xFF0F172A);
            holder.itemView.setAlpha(1f);
        }

        holder.ivStatus.setOnClickListener(v -> {
            if (listener != null) listener.onTaskCheck(task, position);
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onTaskDelete(task, position);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;
        ImageView ivStatus, ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTaskItemName);
            tvDate = itemView.findViewById(R.id.tvTaskItemDate);
            ivStatus = itemView.findViewById(R.id.ivTaskStatus);
            ivDelete = itemView.findViewById(R.id.ivTaskDelete);
        }
    }

    public static class TaskItem {
        // Ensure these variables match the field names in your Firestore database exactly!
        public String id;
        public String title;
        public String dateString;
        public boolean completed; // Must be 'completed', not 'isCompleted'

        public TaskItem() {}
    }
}