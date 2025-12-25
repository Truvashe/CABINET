package com.example.cabinetmedicalapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_APPT_ID = "EXTRA_APPT_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        int apptId = intent.getIntExtra(EXTRA_APPT_ID, -1);
        if (apptId == -1) return;

        DatabaseHelper db = new DatabaseHelper(context);
        Cursor c = db.getAppointmentById(apptId);
        if (c.moveToFirst()) {
            String date = c.getString(c.getColumnIndexOrThrow("date"));
            String time = c.getString(c.getColumnIndexOrThrow("time"));
            String reason = c.getString(c.getColumnIndexOrThrow("reason"));
            int patientId = c.getInt(c.getColumnIndexOrThrow("patient_id"));

            String title = context.getString(R.string.notification_title);
            String message = String.format(context.getString(R.string.notification_message_format), date, time, reason);

            NotificationHelper.showNotification(context, apptId, title, message);
        }
        c.close();
    }
}