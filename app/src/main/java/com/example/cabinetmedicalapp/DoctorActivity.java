package com.example.cabinetmedicalapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DoctorActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    int currentUserId = -1;
    androidx.recyclerview.widget.RecyclerView listAppointments;
    ArrayList<Integer> apptIds = new ArrayList<>();
    AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        myDb = new DatabaseHelper(this);
        listAppointments = findViewById(R.id.recycler_appointments_doctor);
        adapter = new AppointmentAdapter(this);
        listAppointments.setAdapter(adapter);
        listAppointments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter.setListener((apptId, position) -> showAppointmentActions(apptId));

        if (getIntent() != null && getIntent().hasExtra("USER_ID")) {
            currentUserId = getIntent().getIntExtra("USER_ID", -1);
        }
        if (currentUserId == -1) {
            Toast.makeText(this, getString(R.string.err_not_logged), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // toolbar setup
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadAppointments();
    }

    private void loadAppointments() {
        apptIds.clear();
        java.util.ArrayList<AppointmentAdapter.AppointmentItem> items = new java.util.ArrayList<>();
        Cursor c = myDb.getAppointmentsByDoctor(currentUserId);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                int patientId = c.getInt(c.getColumnIndexOrThrow("patient_id"));
                String date = c.getString(c.getColumnIndexOrThrow("date"));
                String time = c.getString(c.getColumnIndexOrThrow("time"));
                String reason = c.getString(c.getColumnIndexOrThrow("reason"));
                String status = c.getString(c.getColumnIndexOrThrow("status"));
                // get patient username
                String patientName = "#" + patientId;
                Cursor u = myDb.getUserById(patientId);
                if (u.moveToFirst()) {
                    patientName = u.getString(u.getColumnIndexOrThrow("username"));
                }
                u.close();

                String title = patientName + " — " + date + " " + time;
                items.add(new AppointmentAdapter.AppointmentItem(id, title, reason, status));
                apptIds.add(id);
            } while (c.moveToNext());
        }
        c.close();
        adapter.setItems(items);
    }

    private void showAppointmentActions(int apptId) {
        final String[] options = new String[]{getString(R.string.mark_done), getString(R.string.cancel_appointment), getString(R.string.reprogram)};
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_actions))
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            boolean ok = myDb.updateAppointmentStatus(apptId, "done");
                            if (ok) {
                                // cancel reminder when done
                                ReminderScheduler.cancelReminder(DoctorActivity.this, apptId);
                                Toast.makeText(DoctorActivity.this, getString(R.string.msg_marked_done), Toast.LENGTH_SHORT).show();
                            }
                            loadAppointments();
                        } else if (which == 1) {
                            boolean ok = myDb.updateAppointmentStatus(apptId, "cancelled");
                            if (ok) {
                                ReminderScheduler.cancelReminder(DoctorActivity.this, apptId);
                                Toast.makeText(DoctorActivity.this, getString(R.string.msg_appointment_cancelled), Toast.LENGTH_SHORT).show();
                            }
                            loadAppointments();
                        } else if (which == 2) {
                            showRescheduleDialog(apptId);
                        }
                    }
                }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
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
                        android.content.Intent i = new android.content.Intent(this, LoginActivity.class);
                        i.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRescheduleDialog(int apptId) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_reschedule, null);
        EditText editDate = v.findViewById(R.id.edit_new_date);
        EditText editTime = v.findViewById(R.id.edit_new_time);

        // Add pickers
        editDate.setFocusable(false);
        editTime.setFocusable(false);
        editDate.setOnClickListener(ev -> showDatePicker(editDate));
        editTime.setOnClickListener(ev -> showTimePicker(editTime));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.reprogram))
                .setView(v)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newDate = editDate.getText().toString();
                        String newTime = editTime.getText().toString();
                        if (newDate.isEmpty() || newTime.isEmpty()) {
                            Toast.makeText(DoctorActivity.this, getString(R.string.err_fill_date_time), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ReminderScheduler.cancelReminder(DoctorActivity.this, apptId);
                        boolean ok = myDb.updateAppointmentDateTime(apptId, newDate, newTime);
                        if (ok) {
                            ReminderScheduler.scheduleReminder(DoctorActivity.this, apptId, newDate, newTime);
                            Toast.makeText(DoctorActivity.this, getString(R.string.msg_rescheduled), Toast.LENGTH_SHORT).show();
                        }
                        loadAppointments();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showDatePicker(final EditText target) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int y = cal.get(java.util.Calendar.YEAR);
        int m = cal.get(java.util.Calendar.MONTH);
        int d = cal.get(java.util.Calendar.DAY_OF_MONTH);
        android.app.DatePickerDialog dp = new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            target.setText(dateStr);
        }, y, m, d);
        dp.show();
    }

    private void showTimePicker(final EditText target) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int h = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int min = cal.get(java.util.Calendar.MINUTE);
        android.app.TimePickerDialog tp = new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            target.setText(timeStr);
        }, h, min, true);
        tp.show();
    }
}
