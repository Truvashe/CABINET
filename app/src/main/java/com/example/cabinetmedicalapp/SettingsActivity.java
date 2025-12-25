package com.example.cabinetmedicalapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    RadioGroup rgLead;
    RadioButton rb10, rb30, rb60;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rgLead = findViewById(R.id.rg_lead_time);
        rb10 = findViewById(R.id.rb_10);
        rb30 = findViewById(R.id.rb_30);
        rb60 = findViewById(R.id.rb_60);
        btnSave = findViewById(R.id.btn_save_settings);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int lead = prefs.getInt("reminder_lead_minutes", 30);
        if (lead == 10) rb10.setChecked(true);
        else if (lead == 60) rb60.setChecked(true);
        else rb30.setChecked(true);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected = rb30.isChecked() ? 30 : (rb10.isChecked() ? 10 : 60);
                prefs.edit().putInt("reminder_lead_minutes", selected).apply();
                Toast.makeText(SettingsActivity.this, getString(R.string.msg_settings_saved), Toast.LENGTH_SHORT).show();
            }
        });
    }
}