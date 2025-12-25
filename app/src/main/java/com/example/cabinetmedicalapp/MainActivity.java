package com.example.cabinetmedicalapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Declare the UI elements and the Database helper
    EditText editName, editDate, editTime, editReason;
    Button btnSave;
    DatabaseHelper myDb;
    int currentUserId = -1;
    String currentRole = "";
    String currentUsername = "";
    java.util.ArrayList<Integer> appointmentIds = new java.util.ArrayList<>();
    AppointmentAdapter adapter;
    java.util.ArrayList<AppointmentAdapter.AppointmentItem> apptItems = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the DatabaseHelper
        myDb = new DatabaseHelper(this);

        // Link the Java variables to the XML IDs
        editName = findViewById(R.id.edit_name);
        editDate = findViewById(R.id.edit_date);
        editTime = findViewById(R.id.edit_time);
        editReason = findViewById(R.id.edit_reason);
        btnSave = findViewById(R.id.btn_save);

        // Read logged-in user info
        if (getIntent() != null && getIntent().hasExtra("USER_ID")) {
            currentUserId = getIntent().getIntExtra("USER_ID", -1);
            currentUsername = getIntent().getStringExtra("USERNAME");
            currentRole = getIntent().getStringExtra("ROLE");
        }

        // hide keyboard on date/time clicks handled below
        editDate.setFocusable(false);
        editTime.setFocusable(false);

        androidx.recyclerview.widget.RecyclerView recycler = findViewById(R.id.recycler_appointments);
        adapter = new AppointmentAdapter(this);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter.setListener((apptId, position) -> {
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.cancel_appointment))
                    .setMessage(getString(R.string.cancel_appointment) + " ?")
                    .setPositiveButton(getString(R.string.cancel_appointment), (dialog, which) -> {
                        boolean ok = myDb.updateAppointmentStatus(apptId, "cancelled");
                        if (ok) {
                            ReminderScheduler.cancelReminder(MainActivity.this, apptId);
                            Toast.makeText(MainActivity.this, getString(R.string.msg_appointment_cancelled), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.err_cannot_cancel), Toast.LENGTH_SHORT).show();
                        }
                        loadAppointments();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });

        // Set the click listener for the button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAppointment();
            }
        });

        // FAB to add appointment (booking dialog with pickers)
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab_add_appointment);
        fab.setOnClickListener(v -> {
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(MainActivity.this);
            android.view.View vv = getLayoutInflater().inflate(R.layout.dialog_book_appointment, null);
            b.setTitle(getString(R.string.title_new_appointment));
            b.setView(vv);

            android.widget.EditText edPatient = vv.findViewById(R.id.edit_book_patient);
            android.widget.EditText edDate = vv.findViewById(R.id.edit_book_date);
            android.widget.EditText edTime = vv.findViewById(R.id.edit_book_time);
            android.widget.EditText edReason = vv.findViewById(R.id.edit_book_reason);

            edPatient.setText(currentUsername);

            // date picker
            edDate.setOnClickListener(dv -> showDatePicker(edDate));
            edTime.setOnClickListener(tv -> showTimePicker(edTime));

            b.setPositiveButton(getString(R.string.ok), (d, which) -> {
                editName.setText(edPatient.getText().toString());
                editDate.setText(edDate.getText().toString());
                editTime.setText(edTime.getText().toString());
                editReason.setText(edReason.getText().toString());
                saveAppointment();
            });
            b.setNegativeButton(getString(R.string.cancel), null);
            b.show();
        });

        // also attach pickers to main form fields
        editDate.setOnClickListener(v -> showDatePicker(editDate));
        editTime.setOnClickListener(v -> showTimePicker(editTime));
    }

    private void saveAppointment() {
        String name = editName.getText().toString();
        String date = editDate.getText().toString();
        String time = editTime.getText().toString();
        String reason = editReason.getText().toString();

        // Simple validation to check if fields are empty
        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || reason.isEmpty()) {
            Toast.makeText(MainActivity.this, getString(R.string.err_fill_all), Toast.LENGTH_SHORT).show();
            return;
        }

        // basic date/time format validation: ensure dd/MM/yyyy and HH:mm
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(MainActivity.this, getString(R.string.err_date_format), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!time.matches("\\d{2}:\\d{2}")) {
            Toast.makeText(MainActivity.this, getString(R.string.err_time_format), Toast.LENGTH_SHORT).show();
            return;
        }

        // Find a doctor id (simple: the seeded 'doctor' user)
        int doctorId = -1;
        Cursor c = myDb.getUserByUsername("doctor");
        if (c.moveToFirst()) {
            doctorId = c.getInt(c.getColumnIndexOrThrow("id"));
        }
        c.close();

        if (doctorId == -1) {
            Toast.makeText(this, getString(R.string.msg_no_doctor), Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert into database
        long rowId = myDb.insertAppointment(currentUserId, doctorId, date, time, reason);

        if (rowId != -1L) {
            Toast.makeText(MainActivity.this, getString(R.string.msg_saved), Toast.LENGTH_LONG).show();
            // Schedule local reminder
            int apptId = (int) rowId;
            ReminderScheduler.scheduleReminder(this, apptId, date, time);
            // Clear fields after saving
            editDate.setText("");
            editTime.setText("");
            editReason.setText("");
            loadAppointments();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.err_save_failed), Toast.LENGTH_SHORT).show();
        }
    }

    // Date/Time pickers helpers
    private void showDatePicker(final android.widget.EditText target) {
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

    private void showTimePicker(final android.widget.EditText target) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int h = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int min = cal.get(java.util.Calendar.MINUTE);
        android.app.TimePickerDialog tp = new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            target.setText(timeStr);
        }, h, min, true);
        tp.show();
    }

    private void loadAppointments() {
        appointmentIds.clear();
        apptItems.clear();
        Cursor c = myDb.getAppointmentsByPatient(currentUserId);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String date = c.getString(c.getColumnIndexOrThrow("date"));
                String time = c.getString(c.getColumnIndexOrThrow("time"));
                String reason = c.getString(c.getColumnIndexOrThrow("reason"));
                String status = c.getString(c.getColumnIndexOrThrow("status"));
                String title = getString(R.string.prefix_rdv) + date + " " + time;
                AppointmentAdapter.AppointmentItem item = new AppointmentAdapter.AppointmentItem(id, title, reason, status);
                apptItems.add(item);
                appointmentIds.add(id);
            } while (c.moveToNext());
        }
        c.close();
        if (adapter != null) adapter.setItems(apptItems);
        // ensure UI scroll when no items
        if (apptItems.isEmpty()) {
            // nothing to show - keep UI clean
        }
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
}