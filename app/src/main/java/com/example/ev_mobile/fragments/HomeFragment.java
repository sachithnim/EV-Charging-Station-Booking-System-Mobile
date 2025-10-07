package com.example.ev_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ev_mobile.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private TextView pendingReservations, approvedReservations;
    private MapView mapView;
    private GoogleMap gMap;

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

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

        // Add dummy charging stations
        LatLng station1 = new LatLng(6.9271, 79.8612); // Colombo
        LatLng station2 = new LatLng(6.9344, 79.8428); // Slave Island
        LatLng station3 = new LatLng(6.9100, 79.8828); // Nugegoda

        gMap.addMarker(new MarkerOptions().position(station1).title("EV Station - Colombo"));
        gMap.addMarker(new MarkerOptions().position(station2).title("EV Station - Slave Island"));
        gMap.addMarker(new MarkerOptions().position(station3).title("EV Station - Nugegoda"));

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(station1, 12f));
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