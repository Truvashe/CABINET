package com.example.cabinetmedicalapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_APPT_ID = "appt_id";

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
            
            String title = context.getString(R.string.notification_title);
            String text = context.getString(R.string.notification_text_prefix) + date + " " + time + " - " + reason;
            
            NotificationHelper.showNotification(context, apptId, title, text);
        }
        c.close();
    }
}