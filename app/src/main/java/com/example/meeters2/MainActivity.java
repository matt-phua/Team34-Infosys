package com.example.meeters2;

import android.content.Intent;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
  MainActivity - The home screen of the app
  This activity shows:
  1. User profile and notifications in the app bar
  2. A search bar for finding events/people
  3. Upcoming events in a horizontal scrolling list
  4. Suggested matches in a horizontal scrolling list
  5. Bottom navigation for accessing different sections of the app
 */
public class MainActivity extends AppCompatActivity {
    // Firebase authentication instance
    private FirebaseAuth mAuth;
    
    // UI Components
    private TextView welcomeText;              // Shows welcome message with user's name
    private ImageView profileImage;            // User's profile picture
    private ImageButton logoutButton;          // Logout button
    private ImageButton notificationButton;          // Notification button
    private RecyclerView upcomingEventsRecyclerView;    // List of upcoming events
    private RecyclerView suggestedMatchesRecyclerView;  // List of suggested matches
    private BottomNavigationView bottomNavigation;      // Bottom navigation bar

    //request real time location
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //override for requesting location access
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth for user authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize all UI components by finding them in the layout
        initializeViews();

        setupToolbar();

        // Check if user is signed in, if not redirect to login
        checkUserAuthentication();

        // RecyclerViews for events and matches
        setupRecyclerViews();

        setupBottomNavigation();

        // Set up logout button click listener
        setupLogoutButton();
        // Set an OnClickListener to handle the button click
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to launch NotificationActivity when the button is clicked
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

    }

    // Find the ImageButton for notifications




    /**
     * Initialize all UI components by finding them in the layout
     */
    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        profileImage = findViewById(R.id.profileImage);
        logoutButton = findViewById(R.id.logoutButton);
        notificationButton = findViewById(R.id.notificationsButton);
        upcomingEventsRecyclerView = findViewById(R.id.upcomingEventsRecyclerView);
        suggestedMatchesRecyclerView = findViewById(R.id.suggestedMatchesRecyclerView);
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

    /**
     * Check if user is signed in, if not redirect to login screen
     */
    private void checkUserAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }



        // Update welcome text with user's email
        welcomeText.setText("Welcome, " + currentUser.getEmail() + "!");

        // Update welcome text with user's name (using email username)
        String email = currentUser.getEmail();
        String displayName = email != null ? email.split("@")[0] : "User";
        welcomeText.setText("Hello, " + displayName);
    }


    private void setupRecyclerViews() {
        // Set up horizontal layout managers for both RecyclerViews
        upcomingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestedMatchesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // TODO: Set up proper adapters with real data
        // For now, just set empty adapters
        setupEmptyAdapter(upcomingEventsRecyclerView);
        setupEmptyAdapter(suggestedMatchesRecyclerView);
    }

    /**
     * Helper method to set up an empty adapter for a RecyclerView
     */
    private void setupEmptyAdapter(RecyclerView recyclerView) {
        recyclerView.setAdapter(new RecyclerView.Adapter<EmptyViewHolder>() {
            @NonNull
            @Override
            public EmptyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new EmptyViewHolder(new View(parent.getContext()));
            }

            @Override
            public void onBindViewHolder(@NonNull EmptyViewHolder holder, int position) {}

            @Override
            public int getItemCount() {
                return 0;
            }
        });
    }

    /**
     * Empty ViewHolder class for the empty adapter
     */
    private static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * Set up the bottom navigation with click listeners
     */
    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.navigation_home) {
                    // If already on home
                    return true;
                } else if (itemId == R.id.navigation_events) {
                    // TODO: Navigate to Events screen

                    return true;
                } else if (itemId == R.id.navigation_messages) {
                    // TODO: Navigate to Messages screen
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // TODO: Navigate to Profile screen
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }


    /**
     * Set up the logout button click listener
     */
    private void setupLogoutButton() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user
                mAuth.signOut();
                
                // Redirect to login screen
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        //asks user for location permission
        checkLocationPermission();
    }
}