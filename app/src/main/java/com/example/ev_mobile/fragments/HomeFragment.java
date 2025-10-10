package com.example.ev_mobile.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ev_mobile.R;
import com.example.ev_mobile.services.ChargingStationService;
import com.example.ev_mobile.util.TokenManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;

import static com.example.ev_mobile.services.BookingService.createBooking;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private TextView pendingReservations, approvedReservations;
    private MapView mapView;
    private GoogleMap gMap;
    private Map<String, JSONObject> stationMap = new HashMap<>();

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize TokenManager here
        new TokenManager(requireContext());

        pendingReservations = view.findViewById(R.id.tv_pending_reservations);
        approvedReservations = view.findViewById(R.id.tv_approved_reservations);
        mapView = view.findViewById(R.id.map_view);

        // Set dummy data
        pendingReservations.setText("2 Pending Reservations");
        approvedReservations.setText("3 Approved Reservations");

        // Initialize MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Set marker click listener
        gMap.setOnMarkerClickListener(marker -> {
            String id = (String) marker.getTag();
            if (id != null) {
                showBookingDialog(id);
            }
            return true;
        });

        // Fetch and display charging stations
        fetchStations();
    }

    private void fetchStations() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONArray arr = ChargingStationService.getAll();
                requireActivity().runOnUiThread(() -> {
                    try {
                        stationMap.clear();
                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                        boolean hasStations = false;
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject st = arr.getJSONObject(i);
                            if (!st.optBoolean("isActive", false)) continue;  // Skip inactive
                            String id = st.getString("id");
                            stationMap.put(id, st);
                            LatLng pos = new LatLng(st.getDouble("latitude"), st.getDouble("longitude"));
                            Marker marker = gMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(st.getString("name")));
                            marker.setTag(id);
                            boundsBuilder.include(pos);
                            hasStations = true;
                        }
                        if (hasStations) {
                            gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Error parsing stations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to fetch stations: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showBookingDialog(String stationId) {
        JSONObject station = stationMap.get(stationId);
        if (station == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        try {
            builder.setTitle(station.getString("name"));
        } catch (Exception ignored) {
            builder.setTitle("Book Slot");
        }

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_book_slot, null);
        builder.setView(dialogView);

        TextView tvSchedules = dialogView.findViewById(R.id.tv_schedules);
        TextView tvSlots = dialogView.findViewById(R.id.tv_slots);
        EditText etNic = dialogView.findViewById(R.id.et_nic);
        EditText etName = dialogView.findViewById(R.id.et_name);
        Spinner spinnerSlotId = dialogView.findViewById(R.id.spinner_slot_id);
        EditText etStartTime = dialogView.findViewById(R.id.et_start_time);
        EditText etEndTime = dialogView.findViewById(R.id.et_end_time);

        // Initialize calendars for start and end times
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();

        // Set click listeners for datetime pickers
        etStartTime.setOnClickListener(v -> showDateTimePicker(startCalendar, etStartTime));
        etEndTime.setOnClickListener(v -> showDateTimePicker(endCalendar, etEndTime));

        // Populate schedules
        try {
            StringBuilder sb = new StringBuilder("Schedules:\n");
            JSONArray schedules = station.getJSONArray("schedules");
            for (int i = 0; i < schedules.length(); i++) {
                JSONObject sch = schedules.getJSONObject(i);
                sb.append("Day: ").append(sch.getInt("dayOfWeek"))
                        .append(", Time: ").append(sch.getString("startTime"))
                        .append(" - ").append(sch.getString("endTime"))
                        .append(", Slot Count: ").append(sch.getInt("slotCount")).append("\n");
            }
            tvSchedules.setText(sb.toString());
        } catch (Exception e) {
            tvSchedules.setText("No schedules available");
        }

        // Populate slots text and spinner
        ArrayList<String> slotIds = new ArrayList<>();
        try {
            StringBuilder sb = new StringBuilder("Available Slots:\n");
            JSONArray slots = station.getJSONArray("slots");
            for (int i = 0; i < slots.length(); i++) {
                JSONObject slot = slots.getJSONObject(i);
                if (slot.optBoolean("isActive", false)) {
                    String id = slot.getString("id");
                    slotIds.add(id);
                    sb.append("ID: ").append(id)
                            .append(", Code: ").append(slot.getString("code"))
                            .append(", Connector: ").append(slot.getString("connectorType"))
                            .append(", Power: ").append(slot.getInt("powerKw")).append(" kW\n");
                }
            }
            tvSlots.setText(sb.toString());
        } catch (Exception e) {
            tvSlots.setText("No slots available");
        }

        // Set up spinner for slot IDs
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, slotIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSlotId.setAdapter(adapter);

        builder.setPositiveButton("Book", (dialog, which) -> {
            String nic = etNic.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String slotId = spinnerSlotId.getSelectedItem() != null ? spinnerSlotId.getSelectedItem().toString() : "";
            String startTime = etStartTime.getText().toString().trim();
            String endTime = etEndTime.getText().toString().trim();

            if (nic.isEmpty() || name.isEmpty() || slotId.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
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

                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        boolean success = createBooking(body);
                        requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), success ? "Booking created!" : "Failed to create booking", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error preparing booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                calendar.set(Calendar.SECOND, 0);  // Set seconds to 0 for simplicity

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                editText.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePicker.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    // Map lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}