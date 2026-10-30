package com.example.eskwela;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<ScheduleItem> scheduleList;

    public ScheduleAdapter(List<ScheduleItem> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem item = scheduleList.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvTime.setText(item.timeAndRoom);

        // Apply colors based on the item's assigned type
        switch (item.colorType) {
            case "BLUE":
                holder.itemView.setBackgroundResource(R.drawable.bg_schedule_blue);
                holder.viewColorBar.setBackgroundColor(0xFF1D4ED8); // Blue
                break;
            case "ORANGE":
                holder.itemView.setBackgroundResource(R.drawable.bg_schedule_orange);
                holder.viewColorBar.setBackgroundColor(0xFFF59E0B); // Orange
                break;
            case "GREEN":
                holder.itemView.setBackgroundResource(R.drawable.bg_schedule_green);
                holder.viewColorBar.setBackgroundColor(0xFF10B981); // Green
                break;
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;
        View viewColorBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvTime = itemView.findViewById(R.id.tvItemTime);
            viewColorBar = itemView.findViewById(R.id.viewColorBar);
        }
    }

    // Helper class to hold data
    public static class ScheduleItem {
        String title;
        String timeAndRoom;
        String colorType; // "BLUE", "ORANGE", "GREEN"

        public ScheduleItem(String title, String timeAndRoom, String colorType) {
            this.title = title;
            this.timeAndRoom = timeAndRoom;
            this.colorType = colorType;
        }
    }
}