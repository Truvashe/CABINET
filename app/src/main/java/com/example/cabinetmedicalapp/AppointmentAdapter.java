package com.example.cabinetmedicalapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<AppointmentItem> items = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int apptId, int position);
    }

    public static class AppointmentItem {
        public int id;
        public String title;
        public String reason;
        public String status;

        public AppointmentItem(int id, String title, String reason, String status) {
            this.id = id;
            this.title = title;
            this.reason = reason;
            this.status = status;
        }
    }

    public AppointmentAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AppointmentItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvReason.setText(item.reason);
        holder.tvStatus.setText(item.status);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item.id, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvReason, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_appointment_title);
            tvReason = itemView.findViewById(R.id.tv_appointment_reason);
            tvStatus = itemView.findViewById(R.id.tv_appointment_status);
        }
    }
}