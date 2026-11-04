package com.example.eskwela;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DashboardClassAdapter extends RecyclerView.Adapter<DashboardClassAdapter.ViewHolder> {

    private List<ScheduleCardAdapter.ScheduleItem> classList;

    public DashboardClassAdapter(List<ScheduleCardAdapter.ScheduleItem> classList) {
        this.classList = classList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleCardAdapter.ScheduleItem item = classList.get(position);
        holder.tvName.setText(item.className);
        holder.tvTime.setText(item.startTime);

        // Only add room and endTime if they exist
        String details = "";
        if (item.room != null && !item.room.isEmpty()) details += item.room;
        if (item.endTime != null && !item.endTime.isEmpty()) {
            if (!details.isEmpty()) details += " • ";
            details += item.endTime;
        }
        holder.tvDetails.setText(details);

        // Only change the DOT color now, not the whole card background
        if ("ORANGE".equals(item.colorType)) {
            holder.dotView.setBackgroundResource(R.drawable.bg_dot_orange);
        } else if ("GREEN".equals(item.colorType)) {
            holder.dotView.setBackgroundResource(R.drawable.bg_dot_green);
        } else {
            holder.dotView.setBackgroundResource(R.drawable.bg_dot_blue);
        }
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvDetails;
        View dotView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDashClassName);
            tvTime = itemView.findViewById(R.id.tvDashClassTime);
            tvDetails = itemView.findViewById(R.id.tvDashClassDetails);
            dotView = itemView.findViewById(R.id.dashDot);
        }
    }
}