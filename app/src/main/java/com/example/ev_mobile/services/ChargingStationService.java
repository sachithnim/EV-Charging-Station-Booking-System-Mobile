package com.example.ev_mobile.services;

import com.example.ev_mobile.util.TokenManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChargingStationService {
    private static final String BASE_URL = "http://192.168.1.8:5263/api/ChargingStations";  // For emulator; use 192.168.1.8 for physical device

    /**
     * GET all charging stations.
     */
    public static JSONArray getAll() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Add Authorization header if token available (optional; comment out if not needed)
        String token = TokenManager.getToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        InputStreamReader isr;
        if (responseCode >= 200 && responseCode < 300) {
            isr = new InputStreamReader(conn.getInputStream());
        } else {
            isr = new InputStreamReader(conn.getErrorStream());
            Log.e("ChargingStationService", "Error response code: " + responseCode);
        }
        reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        String responseBody = sb.toString();
        Log.d("ChargingStationService", "Response body: " + responseBody);

        // Attempt to parse as JSONArray
        try {
            return new JSONArray(responseBody);
        } catch (Exception e) {
            // If not array, try as JSONObject for error details
            try {
                JSONObject errorObj = new JSONObject(responseBody);
                String errorMsg = errorObj.optString("message", "Unknown error");
                throw new Exception("API error: " + errorMsg + " (code: " + responseCode + ")");
            } catch (Exception je) {
                throw new Exception("Invalid response (not JSON): " + responseBody + " (code: " + responseCode + ")", e);
            }
        }
    }
}