package com.example.ev_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ev_mobile.R;
import com.example.ev_mobile.services.BookingService;
import com.example.ev_mobile.util.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BookingDetailsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ScrollView scrollView;
    private TextView tvBookingId, tvName, tvNic, tvStationId, tvSlotId, tvStartTime, tvEndTime, tvStatus;
    private Button completeBtn;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        // Initialize TokenManager
        new TokenManager(this);

        progressBar = findViewById(R.id.progress_bar);
        scrollView = findViewById(R.id.scroll_view);
        tvBookingId = findViewById(R.id.tv_booking_id);
        tvName = findViewById(R.id.tv_name);
        tvNic = findViewById(R.id.tv_nic);
        tvStationId = findViewById(R.id.tv_station_id);
        tvSlotId = findViewById(R.id.tv_slot_id);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvStatus = findViewById(R.id.tv_status);
        completeBtn = findViewById(R.id.btn_complete);

        bookingId = getIntent().getStringExtra("bookingId");

        fetchBookingDetails();

        completeBtn.setOnClickListener(v -> completeBooking());
    }

    private void fetchBookingDetails() {
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject booking = BookingService.getBookingById(bookingId);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                    try {
                        // Parse and set fields (adjust keys based on your actual JSON structure)
                        tvBookingId.setText("Booking ID: " + booking.optString("id", "N/A"));
                        tvName.setText("Name: " + booking.optString("name", "N/A"));
                        tvNic.setText("NIC: " + booking.optString("nic", "N/A"));
                        tvStationId.setText("Station ID: " + booking.optString("stationId", "N/A"));
                        tvSlotId.setText("Slot ID: " + booking.optString("slotId", "N/A"));

                        // Format times if they are ISO strings
                        String startTime = formatDate(booking.optString("startTime"));
                        tvStartTime.setText("Start Time: " + (startTime.isEmpty() ? "N/A" : startTime));
                        String endTime = formatDate(booking.optString("endTime"));
                        tvEndTime.setText("End Time: " + (endTime.isEmpty() ? "N/A" : endTime));

                        tvStatus.setText("Status: " + booking.optString("status", "N/A"));

                        // Add more fields as needed, e.g., tvCreatedAt.setText("Created At: " + formatDate(booking.optString("createdAt")));

                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to display booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            Date date = inputFormat.parse(isoDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
            return outputFormat.format(date);
        } catch (Exception e) {
            return isoDate;  // Fallback to raw if parsing fails
        }
    }

    private void completeBooking() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                boolean success = BookingService.completeBooking(bookingId);
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Booking completed!", Toast.LENGTH_SHORT).show();
                        // Optionally refresh details
                        fetchBookingDetails();
                    } else {
                        Toast.makeText(this, "Failed to complete booking", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}