package com.example.ev_mobile.activities;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ev_mobile.Models.EVOwner;
import com.example.ev_mobile.R;
import com.example.ev_mobile.db.SQLiteHelper;
import com.example.ev_mobile.services.EVOwnerService;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private SQLiteHelper dbHelper;
    private EditText etNic, etName, etEmail, etPhone,etAddress, etPassword, etConfirmPassword;
    private Button btnSubmitRegister;
    private TextView btnLogin;
    private EVOwnerService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new SQLiteHelper(this);
        apiService = new EVOwnerService(this);

        etNic = findViewById(R.id.et_nic);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSubmitRegister = findViewById(R.id.btn_submit_register);
        btnLogin = findViewById(R.id.btn_login);

        btnSubmitRegister.setOnClickListener(v -> registerEVOwner());

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerEVOwner() {
        String nic = etNic.getText().toString();
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();
        String address = etAddress.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        EVOwner owner = new EVOwner(nic, name, email, phone, address, password, true);
        dbHelper.insertEVOwner(owner);

        apiService.registerEVOwner(owner, new EVOwnerService.ApiCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });



    }
}