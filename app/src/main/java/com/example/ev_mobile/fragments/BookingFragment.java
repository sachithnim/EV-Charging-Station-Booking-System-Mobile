package com.example.ev_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.ev_mobile.R;

public class BookingFragment extends Fragment {

    private EditText etStationName, etVehicleNo, etDate, etTime, etDuration;
    private Button btnBook;

    public BookingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        etStationName = view.findViewById(R.id.et_station_name);
        etVehicleNo = view.findViewById(R.id.et_vehicle_no);
        etDate = view.findViewById(R.id.et_date);
        etTime = view.findViewById(R.id.et_time);
        etDuration = view.findViewById(R.id.et_duration);
        btnBook = view.findViewById(R.id.btn_book);

        btnBook.setOnClickListener(v -> {
            String station = etStationName.getText().toString();
            Toast.makeText(getContext(), "Booking submitted for " + station, Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}