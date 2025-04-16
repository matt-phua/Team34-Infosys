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

import com.google.android.material.button.MaterialButton;

/**
 MainActivity - The home screen of the app
 This activity shows:
 1. User profile and notifications in the app bar
 2. A search bar for finding events/people
 3. Upcoming events in a horizontal scrolling list
 4. Suggested matches in a horizontal scrolling list
 5. Bottom navigation for accessing different sections of the app
 */
public class MainActivity extends BaseActivity {
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
    private TextView meetingStatusText;
    private View emptyMatchesView;
    private MaterialButton changeMeetingStatusButton;
    private TextView refreshMatchesButton;
    private MaterialButton refreshEmptyButton;
    private View suggestedMatchesContainer;

    //request real time location
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static final String TAG = "MainActivity";

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
                // Check if user has meeting status enabled
                FirebaseFirestore.getInstance().collection("users").document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean isMeeting = documentSnapshot.getBoolean("isMeeting");
                            if (isMeeting == null || isMeeting) {
                                // Update location and find matches since meeting is enabled
                                updateUserLocationAndFindMatches();
                            }
                        }
                    });
            } else {
                Toast.makeText(this, "Permission denied. Cannot show nearby users.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupToolbar();
        setupBottomNavigation();


        // Initialize Firebase Auth for user authentication
        mAuth = FirebaseAuth.getInstance();


        // Check if user is signed in, if not redirect to login
        checkUserAuthentication();

        // RecyclerViews for events and matches
        setupRecyclerViews();
    }

    // Find the ImageButton for notifications


    /**
     * Initialize all UI components by finding them in the layout
     */
    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        profileImage = findViewById(R.id.profileImage);
        logoutButton = findViewById(R.id.logoutButton);
        notificationButton = findViewById(R.id.notificationButton);
        suggestedMatchesRecyclerView = findViewById(R.id.suggestedMatchesRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        meetingStatusText = findViewById(R.id.meetingStatusText);
        emptyMatchesView = findViewById(R.id.emptyMatchesView);
        changeMeetingStatusButton = findViewById(R.id.changeMeetingStatusButton);
        refreshMatchesButton = findViewById(R.id.refreshMatchesButton);
        refreshEmptyButton = findViewById(R.id.refreshEmptyButton);
        suggestedMatchesContainer = findViewById(R.id.suggestedMatchesContainer);
        
        // Setup click listeners only if views exist
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
        
        if (notificationButton != null) {
            notificationButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }
        
        if (changeMeetingStatusButton != null) {
            changeMeetingStatusButton.setOnClickListener(v -> {
                toggleMeetingStatus();
            });
        }
        
        if (refreshMatchesButton != null) {
            refreshMatchesButton.setOnClickListener(v -> {
                refreshMatches();
            });
        }
        
        if (refreshEmptyButton != null) {
            refreshEmptyButton.setOnClickListener(v -> {
                refreshMatches();
            });
        }
    }


    /**
     * Set up the toolbar with custom styling
     */
    @Override
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            
            welcomeText = findViewById(R.id.welcomeText);
            if (welcomeText != null) {
                loadUserName();
            }
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

        // Get user data from Firestore to check meeting status
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && suggestedMatchesRecyclerView != null) {
                    // Get the user's meeting status
                    Boolean isMeeting = documentSnapshot.getBoolean("isMeeting");
                    
                    // Default to true if not set
                    if (isMeeting == null) {
                        isMeeting = true;
                        // Update the default value in Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("isMeeting", true);
                        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                            .update(updates);
                    }
                    
                    // Show or hide the suggested matches based on meeting status
                    if (isMeeting) {
                        suggestedMatchesRecyclerView.setVisibility(View.VISIBLE);
                        View suggestedMatchesHeader = findViewById(R.id.suggestedMatchesHeader);
                        if (suggestedMatchesHeader != null) {
                            suggestedMatchesHeader.setVisibility(View.VISIBLE);
                        }
                        
                        // Get user location and find nearby users 
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            updateUserLocationAndFindMatches();
                        } else {
                            checkLocationPermission();
                        }
                    } else {
                        // Hide the suggested matches section
                        suggestedMatchesRecyclerView.setVisibility(View.GONE);
                        View suggestedMatchesHeader = findViewById(R.id.suggestedMatchesHeader);
                        if (suggestedMatchesHeader != null) {
                            suggestedMatchesHeader.setVisibility(View.GONE);
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e("MainActivity", "Error reading user data", e);
            });

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
    }

    private void updateUserLocationAndFindMatches() {
        try {
            // Clear existing nearby users first to avoid showing old matches
            if (nearbyUsers != null) {
                nearbyUsers.clear();
                if (nearbyUserAdapter != null) {
                    nearbyUserAdapter.notifyDataSetChanged();
                }
            }
            
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String currentUserId = mAuth.getCurrentUser().getUid();

                    GeoFirestore geoFirestore = new GeoFirestore(db.collection("users"));
                    geoFirestore.setLocation(currentUserId, new GeoPoint(location.getLatitude(), location.getLongitude()), exception -> {
                        if (exception == null) {
                            Log.d("GeoFirestore", "✅ Location set successfully!");
                            Toast.makeText(MainActivity.this, "Location updated!", Toast.LENGTH_SHORT).show();

                            // Start nearby user query
                            findNearbyUsers(location);
                        } else {
                            Log.e("GeoFirestore", "❌ Failed to set location", exception);
                            Toast.makeText(MainActivity.this, "Failed to update location", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.w("MainActivity", "Location was null");
                }
            }).addOnFailureListener(e -> {
                Log.e("MainActivity", "Error getting location", e);
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error accessing location services", e);
            Toast.makeText(this, "Unable to access location services", Toast.LENGTH_SHORT).show();
        }
    }

    private void findNearbyUsers(Location currentLocation) {
        Log.d("GeoQuery", "Starting nearby user query from location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        GeoFirestore geoFirestore = new GeoFirestore(usersRef);

        GeoPoint center = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

        // Set radius to 5 km as required
        geoFirestore.queryAtLocation(center, 5.0) // Radius in km
                .addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                    @Override
                    public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint location) {
                        Log.d("GeoQuery", "✅ Document ENTERED: " + documentSnapshot.getId());
                        Log.d("GeoQuery", "Data: " + documentSnapshot.getData());
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (documentSnapshot.getId().equals(currentUserId)) return; // Skip current user

                        // Fetch first and last name
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        Log.d("NearbyDebug", "User ID: " + documentSnapshot.getId());
                        Log.d("NearbyDebug", "First Name: " + firstName);
                        Log.d("NearbyDebug", "Last Name: " + lastName);

                        // Handle null values
                        if (firstName == null) firstName = "";
                        if (lastName == null) lastName = "";

                        String name = firstName + " " + lastName;
                        
                        // Calculate distance between current user and nearby user
                        double distance = calculateDistance(
                            currentLocation.getLatitude(), 
                            currentLocation.getLongitude(),
                            location.getLatitude(),
                            location.getLongitude()
                        );
                        
                        NearbyUser user = new NearbyUser(documentSnapshot.getId(), name, distance);

                        // Avoid duplicates
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

                        // Hide empty state if we have at least one match
                        if (nearbyUsers.size() > 0 && emptyMatchesView != null) {
                            emptyMatchesView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onDocumentExited(DocumentSnapshot documentSnapshot) {
                        Log.d("GeoQuery", "User exited: " + documentSnapshot.getId());
                    }

                    @Override
                    public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint location) {
                        Log.d("GeoQuery", "User moved: " + documentSnapshot.getId());
                    }

                    @Override
                    public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint location) {
                        Log.d("GeoQuery", "User changed: " + documentSnapshot.getId());
                    }

                    @Override
                    public void onGeoQueryReady() {
                        Log.d("GeoQuery", "✅ GeoQuery is ready. All initial data loaded.");
                        
                        // Show empty state if no matches found
                        if (nearbyUsers.size() == 0 && emptyMatchesView != null) {
                            emptyMatchesView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onGeoQueryError(Exception exception) {
                        Log.e("GeoQuery", "❌ GeoQuery Error: " + exception.getMessage(), exception);
                    }
                });
    }

    /**
     * Calculate distance between two points using the Haversine formula
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    private void setupRecyclerViews() {
        // Check if RecyclerView exists
        if (suggestedMatchesRecyclerView == null) return;
        
        // Initialize the recycler views with empty adapters initially
        suggestedMatchesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Initialize nearby users adapter
        nearbyUserAdapter = new NearbyUserAdapter(nearbyUsers);
        suggestedMatchesRecyclerView.setAdapter(nearbyUserAdapter);
        
        // Check if we should show or hide the matches section based on meeting status
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isMeeting = documentSnapshot.getBoolean("isMeeting");
                        
                        if (isMeeting == null || isMeeting) {
                            // Show the suggested matches section if meeting is enabled
                            suggestedMatchesRecyclerView.setVisibility(View.VISIBLE);
                            View suggestedMatchesHeader = findViewById(R.id.suggestedMatchesHeader);
                            if (suggestedMatchesHeader != null) {
                                suggestedMatchesHeader.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Hide the suggested matches section if meeting is disabled
                            suggestedMatchesRecyclerView.setVisibility(View.GONE);
                            View suggestedMatchesHeader = findViewById(R.id.suggestedMatchesHeader);
                            if (suggestedMatchesHeader != null) {
                                suggestedMatchesHeader.setVisibility(View.GONE);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error checking meeting status", e);
                });
        }
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
            public void onBindViewHolder(@NonNull EmptyViewHolder holder, int position) {
            }

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
        if (bottomNavigation == null) return;

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                return true;
                } else if (itemId == R.id.navigation_events) {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                    startActivity(intent);
                finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                finish();
                    return true;
                }
                return false;
        });

        
        // Set the home item as selected
        bottomNavigation.setSelectedItemId(R.id.navigation_home);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
        
        // Refresh the meeting status when returning to MainActivity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isMeeting = documentSnapshot.getBoolean("isMeeting");
                        
                        // Make sure the UI components exist before updating them
                        if (suggestedMatchesRecyclerView != null) {
                            if (isMeeting == null || isMeeting) {
                                // Show the suggested matches section
                                if (suggestedMatchesContainer != null) {
                                    suggestedMatchesContainer.setVisibility(View.VISIBLE);
                                }
                                
                                // Update UI
                                updateMeetingStatusUI(true);
                                
                                // Update location and find matches if permission granted
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                                    updateUserLocationAndFindMatches();
                                }
                            } else {
                                // Hide the suggested matches section
                                if (suggestedMatchesContainer != null) {
                                    suggestedMatchesContainer.setVisibility(View.GONE);
                                }
                                
                                // Update UI
                                updateMeetingStatusUI(false);
                            }
                        }
                        
                        // Refresh user name in toolbar
                        loadUserName();
                        
                        // Load meeting requests
                        loadMeetingRequests();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle firebase access errors gracefully
                    Log.e(TAG, "Error getting user data", e);
                    loadUserName(); // Still try to load the username with fallback
                });
        }
    }

    private void loadMeetingRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        String currentUserId = currentUser.getUid();
        
        // Query for requests where the current user is the sender or receiver
        FirebaseFirestore.getInstance().collection("meetingRequests")
            .whereEqualTo("senderId", currentUserId)
            .get()
            .addOnSuccessListener(sentRequestsSnapshot -> {
                List<MeetingRequest> requests = new ArrayList<>();
                for (DocumentSnapshot document : sentRequestsSnapshot.getDocuments()) {
                    MeetingRequest request = document.toObject(MeetingRequest.class);
                    if (request != null) {
                        requests.add(request);
                    }
                }
                
                // Now get requests where user is the receiver
                FirebaseFirestore.getInstance().collection("meetingRequests")
                    .whereEqualTo("receiverId", currentUserId)
                    .get()
                    .addOnSuccessListener(receivedRequestsSnapshot -> {
                        for (DocumentSnapshot document : receivedRequestsSnapshot.getDocuments()) {
                            MeetingRequest request = document.toObject(MeetingRequest.class);
                            if (request != null) {
                                requests.add(request);
                            }
                        }
                        
                        // Update badge count or notification for the bottom navigation
                        int pendingRequests = 0;
                        for (MeetingRequest request : requests) {
                            if (request.getStatus().equals("pending") && 
                                request.getReceiverId().equals(currentUserId)) {
                                pendingRequests++;
                            }
                        }
                        
                        // You can update a badge or notification here if needed
                        if (pendingRequests > 0) {
                            Toast.makeText(MainActivity.this, 
                                "You have " + pendingRequests + " pending meeting requests", 
                                Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error loading received requests", e);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e("MainActivity", "Error loading sent requests", e);
            });
    }

    /**
     * Toggle the user's meeting status between available and unavailable
     */
    private void toggleMeetingStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Boolean isMeeting = documentSnapshot.getBoolean("isMeeting");
                    
                    // Toggle the meeting status
                    boolean newStatus = isMeeting == null || !isMeeting;
                    
                    // Update in Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("isMeeting", newStatus);
                    
                    FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            // Update UI
                            updateMeetingStatusUI(newStatus);
                            
                            // Show or hide matches based on new status
                            if (newStatus) {
                                if (suggestedMatchesContainer != null) {
                                    suggestedMatchesContainer.setVisibility(View.VISIBLE);
                                }
                                
                                // Refresh matches
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                        == PackageManager.PERMISSION_GRANTED) {
                                    updateUserLocationAndFindMatches();
                                } else {
                                    checkLocationPermission();
                                }
                            } else {
                                if (suggestedMatchesContainer != null) {
                                    suggestedMatchesContainer.setVisibility(View.GONE);
                                }
                            }
                            
                            // Show status change notification
                            Toast.makeText(MainActivity.this, 
                                newStatus ? "You are now available for meetings" : "You are now unavailable for meetings", 
                                Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("MainActivity", "Error updating meeting status", e);
                            Toast.makeText(MainActivity.this, "Failed to update meeting status", Toast.LENGTH_SHORT).show();
                        });
                }
            });
    }

    /**
     * Update the meeting status UI elements
     */
    private void updateMeetingStatusUI(boolean isMeeting) {
        if (meetingStatusText != null) {
            meetingStatusText.setText(isMeeting ? 
                "You are available for meetings" : 
                "You are not available for meetings");
        }
        
        if (changeMeetingStatusButton != null) {
            changeMeetingStatusButton.setText(isMeeting ? "Disable" : "Enable");
        }
    }

    /**
     * Refresh the suggested matches list
     */
    private void refreshMatches() {
        // Clear the existing matches
        if (nearbyUsers != null) {
            nearbyUsers.clear();
            if (nearbyUserAdapter != null) {
                nearbyUserAdapter.notifyDataSetChanged();
            }
        }
        
        // Show loading state
        if (emptyMatchesView != null) {
            emptyMatchesView.setVisibility(View.GONE);
        }
        
        // Check for location permission and update
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Finding nearby matches...", Toast.LENGTH_SHORT).show();
            updateUserLocationAndFindMatches();
        } else {
            checkLocationPermission();
        }
    }

}


