package com.example.meeters2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NotificationActivity extends AppCompatActivity {
    private TextView nameText;

    private BottomNavigationView bottomNavigation;      // Bottom navigation bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setContentView(R.layout.activity_notification);  // Make sure to replace with your actual layout file







        // Initialize all UI components by finding them in the layout
        initializeViews();

        setupToolbar();

        // Check if user is signed in, if not redirect to login
        //checkUserAuthentication();

        // RecyclerViews for events and matches
        //setupRecyclerViews();

        setupBottomNavigation();

        // Set up logout button click listener
        //setupLogoutButton();
    }
    private void initializeViews() {
        //nameText = findViewById(R.id.nameText);
//      profileImage = findViewById(R.id.profileImage);
//      logoutButton = findViewById(R.id.logoutButton);
//      pcomingEventsRecyclerView = findViewById(R.id.upcomingEventsRecyclerView);
//      suggestedMatchesRecyclerView = findViewById(R.id.suggestedMatchesRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    /**
     * Set up the toolbar with custom styling
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    //String displayName = email != null ? email.split("@")[0] : "User";
    //nameText.setText(displayName);

    private void setupBottomNavigation() {
        //BottomNavigationView bottomNavigation = null;
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                }  else if (itemId == R.id.navigation_profile) {
                    Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}