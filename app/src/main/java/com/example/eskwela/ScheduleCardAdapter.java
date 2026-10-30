package com.example.eskwela;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleCardAdapter extends RecyclerView.Adapter<ScheduleCardAdapter.ViewHolder> {

    private List<ScheduleItem> scheduleList;
    private OnScheduleLongClickListener longClickListener;

    public interface OnScheduleLongClickListener {
        void onScheduleLongClick(ScheduleItem item, int position);
    }

    public ScheduleCardAdapter(List<ScheduleItem> scheduleList, OnScheduleLongClickListener longClickListener) {
        this.scheduleList = scheduleList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem item = scheduleList.get(position);
        holder.tvTitle.setText(item.className);
        holder.tvTime.setText(item.startTime + " - " + item.endTime + " · " + item.room);

        switch (item.colorType) {
            case "ORANGE":
                holder.itemView.setBackgroundResource(R.drawable.bg_schedule_orange);
                holder.viewColorBar.setBackgroundColor(0xFFF59E0B);
                break;
            case "GREEN":
                holder.itemView.setBackgroundResource(R.drawable.bg_schedule_green);
                holder.viewColorBar.setBackgroundColor(0xFF10B981);
                break;
            case "BLUE":
            default:
                holder.itemView.setBackgroundResource(R.drawable.bg_schedule_blue);
                holder.viewColorBar.setBackgroundColor(0xFF1D4ED8);
                break;
        }

        // Set Long Click Listener for deletion
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onScheduleLongClick(item, position);
            }
            return true;
        });
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

    public static class ScheduleItem {
        public String id;
        public String className;
        public String classCode;
        public String school;
        public String startTime;
        public String endTime;
        public String room;
        public String instructor;
        public String dateString;
        public String colorType;

        public ScheduleItem() {}
    }
}