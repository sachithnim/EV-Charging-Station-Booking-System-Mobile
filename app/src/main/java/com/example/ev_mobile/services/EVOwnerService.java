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
                String token = getToken(); // From SharedPreferences
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                if (body != null) {
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(body.toString().getBytes());
                    os.flush();
                    os.close();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    handler.post(() -> callback.onSuccess(response.toString()));
                    Log.d("ApiService", "Response Code: " + responseCode);
                    Log.d("ApiService", "Full Response: " + response.toString());
                } else {
                    handler.post(() -> callback.onFailure("Error: " + responseCode));
                }

            } catch (Exception e) {
                handler.post(() -> callback.onFailure(e.getMessage()));
            }
        });
    }

    // EV Owner Login (get JWT)
    public void loginEVOwner(String nic, ApiCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("NIC", nic);
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
