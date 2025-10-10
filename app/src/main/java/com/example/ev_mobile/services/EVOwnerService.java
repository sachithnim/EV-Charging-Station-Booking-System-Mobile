package com.example.ev_mobile.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.ev_mobile.Models.EVOwner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EVOwnerService {
    private static final String BASE_URL = "http://192.168.1.8:5263/api";
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EVOwnerService(Context context) {
        this.context = context;
    }

    // Generic method to make API request
    private void makeRequest(String endpoint, String method, JSONObject body, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                // Optional token
                String token = getToken();
                if (token != null && !endpoint.equals("/EVOwners")) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                // Send body if exists
                if (body != null) {
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(body.toString().getBytes());
                    os.flush();
                    os.close();
                }

                int responseCode = conn.getResponseCode();
                BufferedReader br;
                if (responseCode >= 200 && responseCode < 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                if (responseCode >= 200 && responseCode < 300) {
                    String result = response.toString();
                    handler.post(() -> callback.onSuccess(result));
                    Log.d("ApiService", "✅ Success: " + result);
                } else {
                    String errorMsg = "Error " + responseCode + ": " + response.toString();
                    handler.post(() -> callback.onFailure(errorMsg));
                    Log.e("ApiService", "❌ Failure: " + errorMsg);
                }

            } catch (Exception e) {
                handler.post(() -> callback.onFailure("Exception: " + e.getMessage()));
                Log.e("ApiService", "Exception", e);
            }
        });
    }



    // EV Owner Login (get JWT)
    public void loginEVOwner(String nic, String password, ApiCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("NIC", nic);
            body.put("Password", password);
        } catch (Exception e) {}
        makeRequest("/auth/evowner-login", "POST", body, callback);
    }

    // Station Operator Login
    public void loginOperator(String username, String password, ApiCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("Username", username);
            body.put("Password", password);
        } catch (Exception e) {}
        makeRequest("/auth/login", "POST", body, callback);
    }

    public void registerEVOwner(EVOwner owner, ApiCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("NIC", owner.getNic());
            body.put("Name", owner.getName());
            body.put("Email", owner.getEmail());
            body.put("Phone", owner.getPhone());
            body.put("Address", owner.getAddress());
            body.put("Password", owner.getPassword());
            body.put("IsActive", owner.isActive());
        } catch (Exception e) {}
        makeRequest("/EVOwners", "POST", body, callback);

    }


    // Get Profile
    public void getProfile(ApiCallback callback) {
        makeRequest("/profile", "GET", null, callback);
    }

    private String getToken() {
        // From SharedPreferences
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("token", null);
    }

    public void saveToken(String token) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString("token", token).apply();
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }
}
