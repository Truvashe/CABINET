package com.example.cabinetmedicalapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderScheduler {
    // schedule reminder timeBeforeMinutes minutes before appointment (read from preferences)

    public static void scheduleReminder(Context context, int apptId, String date, String time) {
        String when = date + " " + time;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date dt = sdf.parse(when);
            if (dt == null) return;

            android.content.SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
            int REMINDER_MINUTES = prefs.getInt("reminder_lead_minutes", 30);

            long triggerAt = dt.getTime() - REMINDER_MINUTES * 60 * 1000L;
            long now = System.currentTimeMillis();
            if (triggerAt < now) {
                // If already past, set a short delay (10s) notification
                triggerAt = now + 10 * 1000L;
            }

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra(ReminderReceiver.EXTRA_APPT_ID, apptId);
            PendingIntent pi = PendingIntent.getBroadcast(context, apptId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (am != null) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void cancelReminder(Context context, int apptId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, apptId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) {
            am.cancel(pi);
        }
    }
}