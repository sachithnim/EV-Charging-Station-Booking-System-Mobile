package com.example.ev_mobile.activities;

import android.os.Bundle;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ev_mobile.fragments.BookingFragment;
import com.example.ev_mobile.fragments.HomeFragment;
import com.example.ev_mobile.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.ev_mobile.R;

public class DashboardActivity extends AppCompatActivity {

    private Toolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load Home by default
        loadFragment(new HomeFragment());
        topAppBar.setTitle("Home");

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "Home";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "Profile";
            } else if (itemId == R.id.booking) {
                selectedFragment = new BookingFragment();
                title = "Bookings";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                topAppBar.setTitle(title);
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply padding only for the top (status bar)
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}