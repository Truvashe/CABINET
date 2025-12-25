package com.example.cabinetmedicalapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    
    private Context context;
    private ArrayList<UserItem> items = new ArrayList<>();
    private Listener listener;

    public interface Listener {
        void onChangeRole(int userId);
        void onResetPassword(int userId);
        void onDelete(int userId);
    }

    public static class UserItem {
        int id;
        String username;
        String role;

        public UserItem(int id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }
    }

    public UserAdapter(Context context) {
        this.context = context;
    }

    public void setItems(ArrayList<UserItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserItem item = items.get(position);
        holder.tvUsername.setText(item.username);
        holder.tvRole.setText(item.role);
        
        holder.btnActions.setOnClickListener(v -> {
            if (listener != null) {
                showActionsDialog(item.id);
            }
        });
    }

    private void showActionsDialog(int userId) {
        final String[] options = {
            context.getString(R.string.option_modify_role),
            context.getString(R.string.option_reset_password),
            context.getString(R.string.option_delete_user)
        };
        
        new android.app.AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.title_actions))
            .setItems(options, (dialog, which) -> {
                if (which == 0 && listener != null) {
                    listener.onChangeRole(userId);
                } else if (which == 1 && listener != null) {
                    listener.onResetPassword(userId);
                } else if (which == 2 && listener != null) {
                    listener.onDelete(userId);
                }
            })
            .show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRole;
        Button btnActions;

        ViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRole = itemView.findViewById(R.id.tv_role);
            btnActions = itemView.findViewById(R.id.btn_actions);
        }
    }
}