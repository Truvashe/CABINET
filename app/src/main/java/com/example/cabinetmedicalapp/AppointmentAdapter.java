package com.example.cabinetmedicalapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {
    public interface Listener {
        void onItemClicked(int apptId, int position);
    }

    private final Context ctx;
    private final ArrayList<AppointmentItem> items = new ArrayList<>();
    private Listener listener;

    public AppointmentAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void setItems(ArrayList<AppointmentItem> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.appointment_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AppointmentItem it = items.get(position);
        holder.tvTitle.setText(it.title);
        holder.tvReason.setText(it.reason);
        holder.tvStatus.setText(it.status);
        // set status visual
        if ("done".equalsIgnoreCase(it.status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_chip);
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ctx.getResources().getColor(R.color.chip_done)));
        } else if ("cancelled".equalsIgnoreCase(it.status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_chip);
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ctx.getResources().getColor(R.color.chip_cancelled)));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_chip);
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ctx.getResources().getColor(R.color.chip_scheduled)));
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClicked(it.id, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView tvTitle, tvReason, tvStatus;

        public VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
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
}