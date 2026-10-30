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

public class DashboardTaskAdapter extends RecyclerView.Adapter<DashboardTaskAdapter.ViewHolder> {

    private List<TaskAdapter.TaskItem> taskList;

    public DashboardTaskAdapter(List<TaskAdapter.TaskItem> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskAdapter.TaskItem task = taskList.get(position);

        holder.tvName.setText(task.title);
        holder.tvDate.setText(task.dateString);

        // Since these are "Due Soon", they should be unchecked
        holder.ivStatus.setImageResource(R.drawable.ic_task_circle);
        holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        holder.tvName.setTextColor(0xFF0F172A);
        holder.itemView.setAlpha(1f);

        // Hide the delete 'X' icon on the dashboard for a cleaner look
        holder.ivDelete.setVisibility(View.GONE);
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
}