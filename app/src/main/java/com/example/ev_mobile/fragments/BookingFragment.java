package com.example.ev_mobile.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ev_mobile.R;
import com.example.ev_mobile.services.BookingService;
import com.example.ev_mobile.services.ProfileService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;

public class BookingFragment extends Fragment {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private List<JSONObject> bookingList = new ArrayList<>();
    private ProfileService profileService;
    private String userNic;

    public BookingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        rvBookings = view.findViewById(R.id.rv_bookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BookingAdapter(bookingList, this::showUpdateDialog, this::confirmCancelBooking);
        rvBookings.setAdapter(adapter);

        profileService = new ProfileService(requireContext());

        fetchProfileAndBookings();

        return view;
    }

    private void fetchProfileAndBookings() {
        profileService.getProfile(new ProfileService.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject profile = new JSONObject(result);
                    userNic = profile.optString("identifier", null);
                    if (userNic != null) {
                        fetchBookings(userNic);
                    } else {
                        Toast.makeText(getContext(), "NIC not found in profile", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error parsing profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to fetch profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBookings(String nic) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONArray bookings = BookingService.getBookingsByOwner(nic);
                requireActivity().runOnUiThread(() -> {
                    bookingList.clear();
                    for (int i = 0; i < bookings.length(); i++) {
                        try {
                            bookingList.add(bookings.getJSONObject(i));
                        } catch (Exception ignored) {}
                    }
                    adapter.notifyDataSetChanged();
                    if (bookingList.isEmpty()) {
                        Toast.makeText(getContext(), "No bookings found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showUpdateDialog(JSONObject booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Booking");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_booking, null);
        builder.setView(dialogView);

        EditText etNic = dialogView.findViewById(R.id.et_nic);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etStationId = dialogView.findViewById(R.id.et_station_id);
        EditText etSlotId = dialogView.findViewById(R.id.et_slot_id);
        EditText etStartTime = dialogView.findViewById(R.id.et_start_time);
        EditText etEndTime = dialogView.findViewById(R.id.et_end_time);

        // Prefill fields
        etNic.setText(booking.optString("nic", ""));
        etName.setText(booking.optString("name", ""));
        etStationId.setText(booking.optString("stationId", ""));
        etSlotId.setText(booking.optString("slotId", ""));
        etStartTime.setText(booking.optString("startTime", ""));
        etEndTime.setText(booking.optString("endTime", ""));

        // DateTime pickers
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        etStartTime.setOnClickListener(v -> showDateTimePicker(startCalendar, etStartTime));
        etEndTime.setOnClickListener(v -> showDateTimePicker(endCalendar, etEndTime));

        builder.setPositiveButton("Update", (dialog, which) -> {
            String nic = etNic.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String stationId = etStationId.getText().toString().trim();
            String slotId = etSlotId.getText().toString().trim();
            String startTime = etStartTime.getText().toString().trim();
            String endTime = etEndTime.getText().toString().trim();

            if (nic.isEmpty() || name.isEmpty() || stationId.isEmpty() || slotId.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject body = new JSONObject();
                body.put("nic", nic);
                body.put("name", name);
                body.put("stationId", stationId);
                body.put("slotId", slotId);
                body.put("startTime", startTime);
                body.put("endTime", endTime);

                String bookingId = booking.getString("id");
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        boolean success = BookingService.updateBooking(bookingId, body);
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(getContext(), "Booking updated!", Toast.LENGTH_SHORT).show();
                                fetchBookings(userNic);  // Refresh list
                            } else {
                                Toast.makeText(getContext(), "Failed to update booking", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error preparing update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDateTimePicker(final Calendar calendar, EditText editText) {
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                editText.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePicker.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void confirmCancelBooking(String bookingId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            boolean success = BookingService.cancelBooking(bookingId);
                            requireActivity().runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(getContext(), "Booking canceled!", Toast.LENGTH_SHORT).show();
                                    fetchBookings(userNic);  // Refresh list
                                } else {
                                    Toast.makeText(getContext(), "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Inner Adapter Class
    private static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

        private final List<JSONObject> bookings;
        private final UpdateClickListener updateListener;
        private final CancelClickListener cancelListener;

        public BookingAdapter(List<JSONObject> bookings, UpdateClickListener updateListener, CancelClickListener cancelListener) {
            this.bookings = bookings;
            this.updateListener = updateListener;
            this.cancelListener = cancelListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            JSONObject booking = bookings.get(position);
            holder.tvBookingId.setText("ID: " + booking.optString("id", "N/A"));
            holder.tvStationId.setText("Station: " + booking.optString("stationId", "N/A"));
            holder.tvSlotId.setText("Slot: " + booking.optString("slotId", "N/A"));
            holder.tvStartTime.setText("Start: " + booking.optString("startTime", "N/A"));
            holder.tvEndTime.setText("End: " + booking.optString("endTime", "N/A"));
            String status = booking.optString("status", "N/A");
            holder.tvStatus.setText("Status: " + status);
            // Add more fields as needed

            if ("Approved".equals(status)) {
                String id = booking.optString("id", null);
                String qrToken = booking.optString("qrToken", null);
                if (id != null && qrToken != null) {
                    String qrUrl = "https://localhost:5173/booking/" + id + "?token=" + qrToken;
                    try {
                        Bitmap qrBitmap = generateQRCode(qrUrl);
                        holder.ivQr.setImageBitmap(qrBitmap);
                        holder.ivQr.setVisibility(View.VISIBLE);
                    } catch (WriterException e) {
                        holder.ivQr.setVisibility(View.GONE);
                    }
                } else {
                    holder.ivQr.setVisibility(View.GONE);
                }
                holder.btnUpdate.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.VISIBLE);
            } else {
                holder.ivQr.setVisibility(View.GONE);
                holder.btnUpdate.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
            }

            holder.btnUpdate.setOnClickListener(v -> {
                updateListener.onUpdateClick(booking);
            });

            holder.btnCancel.setOnClickListener(v -> {
                String id = booking.optString("id", null);
                if (id != null) {
                    cancelListener.onCancelClick(id);
                }
            });
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }

        private Bitmap generateQRCode(String text) throws WriterException {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvBookingId, tvStationId, tvSlotId, tvStartTime, tvEndTime, tvStatus;
            ImageView ivQr;
            Button btnUpdate, btnCancel;

            ViewHolder(View itemView) {
                super(itemView);
                tvBookingId = itemView.findViewById(R.id.tv_booking_id);
                tvStationId = itemView.findViewById(R.id.tv_station_id);
                tvSlotId = itemView.findViewById(R.id.tv_slot_id);
                tvStartTime = itemView.findViewById(R.id.tv_start_time);
                tvEndTime = itemView.findViewById(R.id.tv_end_time);
                tvStatus = itemView.findViewById(R.id.tv_status);
                ivQr = itemView.findViewById(R.id.iv_qr);
                btnUpdate = itemView.findViewById(R.id.btn_update);
                btnCancel = itemView.findViewById(R.id.btn_cancel);
            }
        }
    }

    interface UpdateClickListener {
        void onUpdateClick(JSONObject booking);
    }

    interface CancelClickListener {
        void onCancelClick(String bookingId);
    }
}