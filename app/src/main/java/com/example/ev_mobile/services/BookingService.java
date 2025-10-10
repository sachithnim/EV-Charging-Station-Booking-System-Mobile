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

public class BookingService {
    private static final String BASE_URL = "http://192.168.1.8:5263/api/Bookings";  // Fixed IP (confirm your server)

    /**
     * GET booking details, including token in Authorization header.
     */
    public static JSONObject getBookingById(String bookingId) throws Exception {
        String urlStr = BASE_URL + "/" + bookingId + "/details";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Add Authorization header
        String token = TokenManager.getToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            // Log error for debugging
            Log.e("BookingService", "Error response code: " + responseCode);
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        // Attempt to parse; throw if not JSON
        try {
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            throw new Exception("Invalid JSON response: " + sb.toString(), e);
        }
    }

    public static JSONArray getBookingsByOwner(String nic) throws Exception {
        String urlStr = BASE_URL + "/owner/" + nic;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Add Authorization header
        String token = TokenManager.getToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            Log.e("BookingService", "Error response code: " + responseCode);
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        // Attempt to parse as JSONArray
        try {
            return new JSONArray(sb.toString());
        } catch (Exception e) {
            throw new Exception("Invalid JSON array response: " + sb.toString(), e);
        }
    }

    /**
     * POST to complete booking endpoint, with token header. Assumes no body needed.
     */
    public static boolean completeBooking(String bookingId) throws Exception {
        String urlStr = BASE_URL + "/" + bookingId + "/complete";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");

        // Add Authorization header
        String token = TokenManager.getToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        } else {
            Log.w("BookingService", "No token available for request");
        }

        // If your API expects a JSON body (even empty), enable this; otherwise, comment out
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write("{}".getBytes());  // Empty JSON body
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        // Read response body for debugging (success or error)
        BufferedReader reader;
        InputStreamReader isr;
        if (responseCode >= 200 && responseCode < 300) {
            isr = new InputStreamReader(conn.getInputStream());
        } else {
            isr = new InputStreamReader(conn.getErrorStream());
            Log.e("BookingService", "POST error response code: " + responseCode);
        }
        reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        Log.d("BookingService", "POST response body: " + sb.toString());
        conn.disconnect();

        return (responseCode >= 200 && responseCode < 300);
    }

    public static boolean createBooking(JSONObject body) throws Exception {
        String urlStr = "http://192.168.1.8:5263/api/Bookings";  // Adjust IP if needed
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        // Add Authorization header
        String token = TokenManager.getToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        // Read response body for debugging
        BufferedReader reader;
        InputStreamReader isr;
        if (responseCode >= 200 && responseCode < 300) {
            isr = new InputStreamReader(conn.getInputStream());
        } else {
            isr = new InputStreamReader(conn.getErrorStream());
            Log.e("BookingService", "POST error response code: " + responseCode);
        }
        reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        Log.d("BookingService", "Create booking response body: " + sb.toString());
        conn.disconnect();

        return (responseCode >= 200 && responseCode < 300);
    }

    /**
     * PUT to update booking, with token header.
     */
    public static boolean updateBooking(String bookingId, JSONObject body) throws Exception {
        String urlStr = BASE_URL + "/" + bookingId;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");

        // Add Authorization header
        String token = TokenManager.getToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        // Read response body for debugging
        BufferedReader reader;
        InputStreamReader isr;
        if (responseCode >= 200 && responseCode < 300) {
            isr = new InputStreamReader(conn.getInputStream());
        } else {
            isr = new InputStreamReader(conn.getErrorStream());
            Log.e("BookingService", "PUT error response code: " + responseCode);
        }
        reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        Log.d("BookingService", "Update booking response body: " + sb.toString());
        conn.disconnect();

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("Update failed: " + sb.toString() + " (code: " + responseCode + ")");
        }

        return true;
    }

    /**
     * PATCH to cancel booking, with token header. No body.
     */
    public static boolean cancelBooking(String bookingId) throws Exception {
        String urlStr = BASE_URL + "/" + bookingId + "/cancel";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");

        // Add Authorization header
        String token = TokenManager.getToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        } else {
            Log.w("BookingService", "No token available for request");
        }

        // No body
        conn.connect();

        int responseCode = conn.getResponseCode();
        // Read response body for debugging (success or error)
        BufferedReader reader;
        InputStreamReader isr;
        if (responseCode >= 200 && responseCode < 300) {
            isr = new InputStreamReader(conn.getInputStream());
        } else {
            isr = new InputStreamReader(conn.getErrorStream());
            Log.e("BookingService", "PATCH error response code: " + responseCode);
        }
        reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        Log.d("BookingService", "Cancel response body: " + sb.toString());
        conn.disconnect();

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("Cancel failed: " + sb.toString() + " (code: " + responseCode + ")");
        }

        return true;
    }
}