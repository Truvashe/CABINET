package com.example.cabinetmedicalapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
    public interface Listener {
        void onChangeRole(int userId);
        void onResetPassword(int userId);
        void onDelete(int userId);
    }

    private final Context ctx;
    private final ArrayList<UserItem> items = new ArrayList<>();
    private Listener listener;

    public UserAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void setListener(Listener l) { this.listener = l; }

    public void setItems(ArrayList<UserItem> list) {
        items.clear(); items.addAll(list); notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.user_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UserItem it = items.get(position);
        holder.tvUsername.setText(it.username);
        holder.tvRole.setText(it.role);
        holder.btnChange.setOnClickListener(v -> { if (listener != null) listener.onChangeRole(it.id); });
        holder.btnReset.setOnClickListener(v -> { if (listener != null) listener.onResetPassword(it.id); });
        holder.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(it.id); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRole;
        ImageButton btnChange, btnReset, btnDelete;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRole = itemView.findViewById(R.id.tv_role);
            btnChange = itemView.findViewById(R.id.btn_change_role);
            btnReset = itemView.findViewById(R.id.btn_reset_pass);
            btnDelete = itemView.findViewById(R.id.btn_delete_user);
        }
    }

    public static class UserItem {
        public int id; public String username; public String role;
        public UserItem(int id, String username, String role) { this.id = id; this.username = username; this.role = role; }
    }
}