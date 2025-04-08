package com.example.meeters2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.FieldValue;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.callbacks.GeoQueryDataEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.messaging.FirebaseMessaging;

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
    private List<NearbyUser> nearbyUsers = new ArrayList<>();
    private NearbyUserAdapter nearbyUserAdapter;
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

        //asks user for location permission
        checkLocationPermission();

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

        // Store FCM token in Firestore after login
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .update("fcmToken", token);
                    }
                });

        // Update welcome text with user's name (using email username)
        String email = currentUser.getEmail();
        String displayName = email != null ? email.split("@")[0] : "User";
        welcomeText.setText("Hello, " + displayName);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                    Map<String, Object> locationUpdate = new HashMap<>();
                    locationUpdate.put("location", geoPoint);
                    locationUpdate.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("users").document(currentUser.getUid())
                            .update(locationUpdate)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MainActivity.this, "Location updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Failed to update location", Toast.LENGTH_SHORT).show();
                            });

                    // query for nearby users
                    findNearbyUsers(location);
                }
            });
        }

    }

    private void findNearbyUsers(Location currentLocation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        GeoFirestore geoFirestore = new GeoFirestore(usersRef);

        GeoPoint center = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

        geoFirestore.queryAtLocation(center, 1.0) // Radius in km
                .addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                    @Override
                    public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint location) {
                        Log.d("Nearby", "User found: " + documentSnapshot.getId());

                        // 🔥 You can display these users on the map or in a list
                        String name = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");
                        NearbyUser user = new NearbyUser(documentSnapshot.getId(), name);

                        //Avoid duplicates
                        boolean exists = false;
                        for (NearbyUser u : nearbyUsers) {
                            if (u.getId().equals(user.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            nearbyUsers.add(user);
                            nearbyUserAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override public void onDocumentExited(DocumentSnapshot documentSnapshot) {}
                    @Override public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint location) {}
                    @Override public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint location) {}
                    @Override public void onGeoQueryReady() {}
                    @Override public void onGeoQueryError(Exception exception) {
                        Log.e("GeoFirestore", "GeoQuery Error", exception);
                    }
                });
    }

    private void setupRecyclerViews() {
        // Set up horizontal layout managers for both RecyclerViews
        suggestedMatchesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // TODO: Set up proper adapters with real data
        nearbyUserAdapter = new NearbyUserAdapter(nearbyUsers);
        suggestedMatchesRecyclerView.setAdapter(nearbyUserAdapter);
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

    }
}

