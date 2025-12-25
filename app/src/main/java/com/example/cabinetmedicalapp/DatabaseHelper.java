package com.example.cabinetmedicalapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "MedicalCabinet.db";
    private static final int DATABASE_VERSION = 2;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password_hash";
    private static final String COL_ROLE = "role"; // patient, doctor, admin

    // Appointments table
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String COL_APPT_ID = "id";
    private static final String COL_PATIENT_ID = "patient_id";
    private static final String COL_DOCTOR_ID = "doctor_id";
    private static final String COL_DATE = "date";
    private static final String COL_TIME = "time";
    private static final String COL_REASON = "reason";
    private static final String COL_STATUS = "status"; // scheduled, cancelled, done

    // Timeslots table (optional)
    private static final String TABLE_SLOTS = "timeslots";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " + COL_PASSWORD + " TEXT, " + COL_ROLE + " TEXT)";
        db.execSQL(createUsers);

        // Create appointments table
        String createAppts = "CREATE TABLE " + TABLE_APPOINTMENTS + " (" + COL_APPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PATIENT_ID + " INTEGER, " + COL_DOCTOR_ID + " INTEGER, " + COL_DATE + " TEXT, " + COL_TIME + " TEXT, " +
                COL_REASON + " TEXT, " + COL_STATUS + " TEXT)";
        db.execSQL(createAppts);

        // Create timeslots table (doctor availability)
        String createSlots = "CREATE TABLE " + TABLE_SLOTS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, doctor_id INTEGER, date TEXT, time TEXT, available INTEGER)";
        db.execSQL(createSlots);

        // Seed sample users
        seedUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLOTS);
        onCreate(db);
    }

    /* Users methods */
    private void seedUsers(SQLiteDatabase db) {
        // Insert admin if not exists
        insertUserIfNotExists(db, "admin", hashPassword("admin123"), "admin");
        insertUserIfNotExists(db, "doctor", hashPassword("doc123"), "doctor");
        insertUserIfNotExists(db, "patient", hashPassword("patient123"), "patient");
    }

    private void insertUserIfNotExists(SQLiteDatabase db, String username, String passwordHash, String role) {
        Cursor c = db.rawQuery("SELECT id FROM " + TABLE_USERS + " WHERE username = ?", new String[]{username});
        boolean exists = c.moveToFirst();
        c.close();
        if (!exists) {
            ContentValues cv = new ContentValues();
            cv.put(COL_USERNAME, username);
            cv.put(COL_PASSWORD, passwordHash);
            cv.put(COL_ROLE, role);
            db.insert(TABLE_USERS, null, cv);
        }
    }

    public long createUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, hashPassword(password));
        cv.put(COL_ROLE, role);
        return db.insert(TABLE_USERS, null, cv);
    }

    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username = ?", new String[]{username});
    }

    public Cursor verifyUser(String username, String password) {
        String hashed = hashPassword(password);
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username = ? AND password_hash = ?", new String[]{username, hashed});
    }

    /* Appointments methods */
    public long insertAppointment(int patientId, int doctorId, String date, String time, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PATIENT_ID, patientId);
        cv.put(COL_DOCTOR_ID, doctorId);
        cv.put(COL_DATE, date);
        cv.put(COL_TIME, time);
        cv.put(COL_REASON, reason);
        cv.put(COL_STATUS, "scheduled");
        return db.insert(TABLE_APPOINTMENTS, null, cv);
    }

    public Cursor getAppointmentById(int apptId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS + " WHERE " + COL_APPT_ID + " = ?", new String[]{String.valueOf(apptId)});
    }

    public Cursor getAppointmentsByPatient(int patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS + " WHERE patient_id = ? ORDER BY date, time", new String[]{String.valueOf(patientId)});
    }

    public Cursor getAppointmentsByDoctor(int doctorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS + " WHERE doctor_id = ? ORDER BY date, time", new String[]{String.valueOf(doctorId)});
    }

    public boolean updateAppointmentStatus(int apptId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, status);
        int rows = db.update(TABLE_APPOINTMENTS, cv, COL_APPT_ID + " = ?", new String[]{String.valueOf(apptId)});
        return rows > 0;
    }

    public boolean updateAppointmentDateTime(int apptId, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DATE, date);
        cv.put(COL_TIME, time);
        int rows = db.update(TABLE_APPOINTMENTS, cv, COL_APPT_ID + " = ?", new String[]{String.valueOf(apptId)});
        return rows > 0;
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE id = ?", new String[]{String.valueOf(userId)});
    }

    // User management
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " ORDER BY role, username", null);
    }

    public Cursor getDoctors() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE role = ? ORDER BY username", new String[]{"doctor"});
    }

    public boolean updateUserRole(int userId, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ROLE, role);
        int rows = db.update(TABLE_USERS, cv, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PASSWORD, hashPassword(newPassword));
        int rows = db.update(TABLE_USERS, cv, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USERS, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // Timeslots management
    public long insertTimeslot(int doctorId, String date, String time, int available) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("doctor_id", doctorId);
        cv.put("date", date);
        cv.put("time", time);
        cv.put("available", available);
        return db.insert(TABLE_SLOTS, null, cv);
    }

    public Cursor getTimeslotsByDoctor(int doctorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SLOTS + " WHERE doctor_id = ? ORDER BY date, time", new String[]{String.valueOf(doctorId)});
    }

    public boolean updateTimeslotAvailability(int slotId, int available) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("available", available);
        int rows = db.update(TABLE_SLOTS, cv, "id = ?", new String[]{String.valueOf(slotId)});
        return rows > 0;
    }

    public boolean deleteTimeslot(int slotId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_SLOTS, "id = ?", new String[]{String.valueOf(slotId)});
        return rows > 0;
    }

    public boolean deleteAppointment(int apptId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_APPOINTMENTS, COL_APPT_ID + " = ?", new String[]{String.valueOf(apptId)});
        return rows > 0;
    }

    /* Simple SHA-256 hashing for passwords */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 not available", e);
            return password; // fallback (not ideal)
        }
    }
}