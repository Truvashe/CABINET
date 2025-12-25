package com.example.cabinetmedicalapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    androidx.recyclerview.widget.RecyclerView recyclerUsers;
    Button btnAddUser;
    UserAdapter userAdapter;
    ArrayList<Integer> userIds = new ArrayList<>();
    int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        myDb = new DatabaseHelper(this);
        recyclerUsers = findViewById(R.id.recycler_users);
        userAdapter = new UserAdapter(this);
        recyclerUsers.setAdapter(userAdapter);
        recyclerUsers.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        btnAddUser = findViewById(R.id.btn_add_user);

        loadUsers();

        btnAddUser.setOnClickListener(v -> showAddUserDialog());

        userAdapter.setListener(new UserAdapter.Listener() {
            @Override
            public void onChangeRole(int userId) {
                showChangeRoleDialog(userId);
            }

            @Override
            public void onResetPassword(int userId) {
                showResetPasswordDialog(userId);
            }

            @Override
            public void onDelete(int userId) {
                boolean ok = myDb.deleteUser(userId);
                if (ok) Toast.makeText(AdminActivity.this, getString(R.string.msg_user_deleted), Toast.LENGTH_SHORT).show();
                loadUsers();
            }
        });

        // toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent() != null && getIntent().hasExtra("USER_ID")) {
            currentUserId = getIntent().getIntExtra("USER_ID", -1);
        }

        if (currentUserId == -1) {
            Toast.makeText(this, getString(R.string.err_not_logged), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void loadUsers() {
        userIds.clear();
        ArrayList<UserAdapter.UserItem> items = new ArrayList<>();
        Cursor c = myDb.getAllUsers();
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String username = c.getString(c.getColumnIndexOrThrow("username"));
                String role = c.getString(c.getColumnIndexOrThrow("role"));
                items.add(new UserAdapter.UserItem(id, username, role));
                userIds.add(id);
            } while (c.moveToNext());
        }
        c.close();
        userAdapter.setItems(items);
    }

    private void showAddUserDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        EditText editUser = v.findViewById(R.id.edit_new_username);
        EditText editPass = v.findViewById(R.id.edit_new_password);
        EditText editRole = v.findViewById(R.id.edit_new_role);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_add_user))
                .setView(v)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String u = editUser.getText().toString().trim();
                        String p = editPass.getText().toString();
                        String r = editRole.getText().toString().trim();
                        if (u.isEmpty() || p.isEmpty() || r.isEmpty()) {
                            Toast.makeText(AdminActivity.this, getString(R.string.err_missing_fields), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        long res = myDb.createUser(u, p, r);
                        if (res != -1) {
                            Toast.makeText(AdminActivity.this, getString(R.string.msg_user_added), Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            Toast.makeText(AdminActivity.this, getString(R.string.err_username_exists), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showUserActions(int userId) {
        final String[] options = new String[]{"Modifier rôle", "Réinitialiser mot de passe", "Supprimer utilisateur", "Gérer créneaux (si médecin)"};
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_actions))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showChangeRoleDialog(userId);
                    } else if (which == 1) {
                        showResetPasswordDialog(userId);
                    } else if (which == 2) {
                        boolean ok = myDb.deleteUser(userId);
                        if (ok) Toast.makeText(AdminActivity.this, getString(R.string.msg_user_deleted), Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else if (which == 3) {
                        // open timeslot management only if user is doctor
                        Cursor u = myDb.getUserById(userId);
                        if (u.moveToFirst()) {
                            String role = u.getString(u.getColumnIndexOrThrow("role"));
                            if ("doctor".equalsIgnoreCase(role)) {
                                showTimeslotManager(userId);
                            } else {
                                Toast.makeText(AdminActivity.this, getString(R.string.msg_not_doctor), Toast.LENGTH_SHORT).show();
                            }
                        }
                        u.close();
                    }
                }).show();
    }

    private void showChangeRoleDialog(int userId) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        EditText editRole = v.findViewById(R.id.edit_new_role);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_change_role))
                .setView(v)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    String r = editRole.getText().toString().trim();
                    if (!r.isEmpty()) {
                        boolean ok = myDb.updateUserRole(userId, r);
                        if (ok) Toast.makeText(AdminActivity.this, getString(R.string.msg_role_updated), Toast.LENGTH_SHORT).show();
                        loadUsers();
                    }
                }).setNegativeButton(getString(R.string.cancel), null).show();
    }

    private void showResetPasswordDialog(int userId) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        EditText editPass = v.findViewById(R.id.edit_new_password);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_reset_password))
                .setView(v)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    String p = editPass.getText().toString();
                    if (!p.isEmpty()) {
                        boolean ok = myDb.updateUserPassword(userId, p);
                        if (ok) Toast.makeText(AdminActivity.this, getString(R.string.msg_password_updated), Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(getString(R.string.cancel), null).show();
    }

    private void showTimeslotManager(int doctorId) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_timeslot_manager, null);
        ListView lv = v.findViewById(R.id.list_timeslots);
        EditText editDate = v.findViewById(R.id.edit_slot_date);
        EditText editTime = v.findViewById(R.id.edit_slot_time);

        ArrayList<Integer> slotIds = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        Cursor c = myDb.getTimeslotsByDoctor(doctorId);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String date = c.getString(c.getColumnIndexOrThrow("date"));
                String time = c.getString(c.getColumnIndexOrThrow("time"));
                int avail = c.getInt(c.getColumnIndexOrThrow("available"));
                String availability = avail == 1 ? getString(R.string.status_available) : getString(R.string.status_occupied);
                adapter.add(date + " " + time + " — " + availability);
                slotIds.add(id);
            } while (c.moveToNext());
        }
        c.close();
        lv.setAdapter(adapter);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_timeslot_manager))
                .setView(v)
                .setPositiveButton(getString(R.string.btn_add), (dialog, which) -> {
                    String d = editDate.getText().toString();
                    String t = editTime.getText().toString();
                    if (d.isEmpty() || t.isEmpty()) {
                        Toast.makeText(AdminActivity.this, getString(R.string.err_date_time_required), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long res = myDb.insertTimeslot(doctorId, d, t, 1);
                    if (res != -1) Toast.makeText(AdminActivity.this, getString(R.string.msg_slot_added), Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .setNegativeButton(getString(R.string.btn_close), null);

        AlertDialog dialog = b.show();

        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < slotIds.size()) {
                int slotId = slotIds.get(position);
                // Toggle availability
                Cursor s = myDb.getTimeslotsByDoctor(doctorId);
                // find availability via cursor (inefficient but ok for now)
                int avail = 0;
                if (s.moveToPosition(position)) {
                    avail = s.getInt(s.getColumnIndexOrThrow("available"));
                }
                s.close();
                myDb.updateTimeslotAvailability(slotId, avail == 1 ? 0 : 1);
                Toast.makeText(AdminActivity.this, getString(R.string.msg_availability_changed), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                showTimeslotManager(doctorId);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(getString(R.string.action_logout))
                    .setMessage(getString(R.string.action_logout) + " ?")
                    .setPositiveButton(getString(R.string.action_logout), (dialog, which) -> {
                        Intent i = new Intent(this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
