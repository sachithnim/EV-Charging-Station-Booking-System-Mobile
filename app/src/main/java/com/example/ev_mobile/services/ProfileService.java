package com.example.ev_mobile.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileService {
    private static final String BASE_URL = "http://192.168.1.8:5263/api";
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ProfileService(Context context) {
        this.context = context;
    }

    private String getToken() {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("token", null);
    }

    private void makeRequest(String endpoint, String method, JSONObject body, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                String token = getToken();
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
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (responseCode >= 200 && responseCode < 300)
                                ? conn.getInputStream()
                                : conn.getErrorStream()
                ));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                if (responseCode >= 200 && responseCode < 300) {
                    handler.post(() -> callback.onSuccess(response.toString()));
                } else {
                    handler.post(() -> callback.onFailure("Error " + responseCode + ": " + response));
                }
            } catch (Exception e) {
                Log.e("ProfileService", "Exception", e);
                handler.post(() -> callback.onFailure("Exception: " + e.getMessage()));
            }
        });
    }

    // ✅ Get logged-in user profile
    public void getProfile(ApiCallback callback) {
        makeRequest("/Profile", "GET", null, callback);
    }

    // ✅ Update Profile (using NIC)
    public void updateProfile(String nic, JSONObject updatedData, ApiCallback callback) {
        makeRequest("/EVOwners/" + nic, "PUT", updatedData, callback);
    }

    // ✅ Deactivate account (POST /EVOwners/e/deactivate)
    public void deactivateAccount(String nic,ApiCallback callback) {
        makeRequest("/EVOwners/"+nic+"/deactivate", "POST", null, callback);
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }
}