package com.example.meeters2;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextView nameText;

    private ImageButton logoutButton;          // Logout button

    private FirebaseAuth mAuth;

    private BottomNavigationView bottomNavigation;      // Bottom navigation bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // Find the ToggleButton
        ToggleButton meetingsToggle = findViewById(R.id.meetings_toggle);

        // Set the initial color (Red for OFF state)
        meetingsToggle.setTextColor(Color.RED);

        // Set a listener to detect when the ToggleButton is checked/unchecked
        meetingsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Change color to green when the toggle is ON
                meetingsToggle.setTextColor(Color.GREEN);
            } else {
                // Change color to red when the toggle is OFF
                meetingsToggle.setTextColor(Color.RED);
            }
        });



        // Initialize all UI components by finding them in the layout
        initializeViews();

        setupToolbar();

        // Initialize Firebase Auth for user authentication
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in, if not redirect to login
        //checkUserAuthentication();

        // RecyclerViews for events and matches
        //setupRecyclerViews();

        setupBottomNavigation();

        // Set up logout button click listener
        setupLogoutButton();
    }
    private void initializeViews() {
        //nameText = findViewById(R.id.nameText);
        //profileImage = findViewById(R.id.profileImage);
        logoutButton = findViewById(R.id.logoutButton);
//      pcomingEventsRecyclerView = findViewById(R.id.upcomingEventsRecyclerView);
//      suggestedMatchesRecyclerView = findViewById(R.id.suggestedMatchesRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }
    //String displayName = email != null ? email.split("@")[0] : "User";
    //nameText.setText(displayName);
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

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Already on Profile page
                if (itemId == R.id.navigation_profile) {
                    return true;
                }

                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0); // Smooth transition
                    finish(); // Close profile activity
                    return true;
                }

                return false;
            }
        });

        // Set the selected item explicitly on start (Profile by default)
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
    }

    private void setupLogoutButton() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user
                mAuth.signOut();

                // Redirect to login screen
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }
}
