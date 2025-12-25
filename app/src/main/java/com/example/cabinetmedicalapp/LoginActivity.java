package com.example.cabinetmedicalapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText editUsername, editPassword;
    Button btnLogin, btnRegister;
    DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myDb = new DatabaseHelper(this);

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void login() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.err_login_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor c = myDb.verifyUser(username, password);
        if (c.moveToFirst()) {
            int id = c.getInt(c.getColumnIndexOrThrow("id"));
            String role = c.getString(c.getColumnIndexOrThrow("role"));
            Intent intent;
            if ("patient".equalsIgnoreCase(role)) {
                intent = new Intent(this, MainActivity.class);
            } else if ("doctor".equalsIgnoreCase(role)) {
                intent = new Intent(this, DoctorActivity.class);
            } else { // admin
                intent = new Intent(this, AdminActivity.class);
            }
            intent.putExtra("USER_ID", id);
            intent.putExtra("USERNAME", username);
            intent.putExtra("ROLE", role);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.err_login_failed), Toast.LENGTH_SHORT).show();
        }
        c.close();
    }

    private void register() {
        // Simple registration: open a dialog or another activity. For now, quick inline registration with patient role
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.err_fill_all), Toast.LENGTH_SHORT).show();
            return;
        }
        long res = myDb.createUser(username, password, "patient");
        if (res != -1) {
            Toast.makeText(this, getString(R.string.msg_account_created), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.err_username_exists), Toast.LENGTH_SHORT).show();
        }
    }
}
