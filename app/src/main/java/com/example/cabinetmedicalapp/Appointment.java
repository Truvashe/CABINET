package com.example.cabinetmedicalapp;

public class Appointment {
    private int id;
    private String patientName;
    private String date;
    private String time;

    public Appointment(int id, String patientName, String date, String time) {
        this.id = id;
        this.patientName = patientName;
        this.date = date;
        this.time = time;
    }

    // Getters
    public int getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}