package com.example.ev_mobile.activities;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.ev_mobile.databinding.ActivityLoginBinding;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ev_mobile.R;
import com.example.ev_mobile.db.SQLiteHelper;
import com.example.ev_mobile.Models.EVOwner;
import com.example.ev_mobile.services.EVOwnerService;
import org.json.JSONObject;

import com.example.ev_mobile.R;

public class LoginActivity extends AppCompatActivity {
    private SQLiteHelper dbHelper;
    private EVOwnerService apiService;
    private RadioButton rbEVOwner, rbOperator;
    private EditText etNicUsername, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new SQLiteHelper(this);
        apiService = new EVOwnerService(this);

        rbEVOwner = findViewById(R.id.rb_ev_owner);
        rbOperator = findViewById(R.id.rb_operator);
        etNicUsername = findViewById(R.id.et_nic_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setVisibility(View.GONE); // Show only for EV Owner

        rbEVOwner.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etNicUsername.setHint("NIC");
                etPassword.setVisibility(View.GONE);
                btnRegister.setVisibility(View.VISIBLE);
            }
        });

        rbOperator.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etNicUsername.setHint("Username");
                etPassword.setVisibility(View.VISIBLE);
                btnRegister.setVisibility(View.GONE);
            }
        });

        btnLogin.setOnClickListener(v -> login());

        btnRegister.setOnClickListener(v -> register());
    }

    // Login method
    private void login() {
        String identifier = etNicUsername.getText().toString();
        if (rbEVOwner.isChecked()) {
            // Local check first
            EVOwner owner = dbHelper.getEVOwner(identifier);
            if (owner == null || !owner.isActive()) {
                Toast.makeText(this, "Invalid or inactive account", Toast.LENGTH_SHORT).show();
                return;
            }
            apiService.loginEVOwner(identifier, new EVOwnerService.ApiCallback<String>() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        apiService.saveToken(obj.getString("Token"));
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (rbOperator.isChecked()) {
            String password = etPassword.getText().toString();
            apiService.loginOperator(identifier, password, new EVOwnerService.ApiCallback<String>() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject obj = new JSONObject(response);
                        String token = obj.optString("token");  // Lowercase 't' to match API response
                        if (token.isEmpty()) {
                            token = obj.optString("Token");  // Fallback for uppercase if API changes
                        }
                        if (!token.isEmpty()) {
                            apiService.saveToken(token);
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();  // Optional: Close login activity
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid response: No token found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();  // For debugging
                    }
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Register EV Owner (local + API if needed, but spec is local)
    private void register() {
        // Collect inputs, create EVOwner, insert to DB
        String nic = etNicUsername.getText().toString();
        // ... other fields from dialog or fields
        EVOwner owner = new EVOwner(nic, "Name", "email", "phone", true);
        dbHelper.insertEVOwner(owner);
        Toast.makeText(this, "Registered. Now login.", Toast.LENGTH_SHORT).show();
    }
}