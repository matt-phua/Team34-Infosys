package com.example.meeters2;

import android.app.usage.NetworkStats;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationActivity extends BaseActivity {
    private static final String TAG = "NotificationActivity";
    private TextView nameText;
    private RecyclerView requestRecyclerView;
    private RecyclerView sentRequestRecyclerView;

    private MeetingRequestAdapter requestAdapter;
    private MeetingRequestAdapter sentRequestAdapter;
    private List<MeetingRequest> requestList = new ArrayList<>();
    private List<MeetingRequest> sentRequestList = new ArrayList<>();
    private BottomNavigationView bottomNavigation;
    private View emptyRequestsView;
    private ImageButton homeButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    // Auto-refresh handler
    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupBottomNavigation();
        setupLogoutButton();
        setupSwipeRefresh();
        
        // Load meeting requests
        loadMeetingRequests();
        
        // Setup auto refresh
        refreshRunnable = this::loadMeetingRequests;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadMeetingRequests();
        
        // Start auto-refresh
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-refresh when activity is paused
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        requestRecyclerView = findViewById(R.id.requestRecyclerView);
        sentRequestRecyclerView = findViewById(R.id.sentRequestRecyclerView);
        emptyRequestsView = findViewById(R.id.emptyRequestsView);
        homeButton = findViewById(R.id.homeButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
    
    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadMeetingRequests);
            swipeRefreshLayout.setColorSchemeResources(
                R.color.meeters_purple,
                R.color.meeters_teal,
                R.color.meeters_purple_light
            );
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
        }
    }

    /**
     * Set up the bottom navigation with click listeners
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(NotificationActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(NotificationActivity.this, ProfileActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_events) {
                return true;
            }
            
            return false;
        });
    }

    private void setupRecyclerView() {
        if (requestRecyclerView == null) return;
        
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestAdapter = new MeetingRequestAdapter(this, requestList);
        requestRecyclerView.setAdapter(requestAdapter);
        sentRequestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sentRequestAdapter = new MeetingRequestAdapter(this, sentRequestList);
        sentRequestRecyclerView.setAdapter(sentRequestAdapter);
    }

    private void loadMeetingRequests() {
        swipeRefreshLayout.setRefreshing(true);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            handleNoRequests();
            return;
        }

        // Clear previous list
        sentRequestList.clear();
        sentRequestAdapter.notifyDataSetChanged();

        // Query where user is the receiver
        db.collection("meetingRequests")
                .whereEqualTo("receiverId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    for (DocumentSnapshot document : queryDocuments) {
                        MeetingRequest request = document.toObject(MeetingRequest.class);
                        if (request != null) {
                            sentRequestList.add(request);
                        }
                    }

                    // Notify the adapter
                    sentRequestAdapter.notifyDataSetChanged();

                    // Show the sentRequestRecyclerView if not empty
                    if (!sentRequestList.isEmpty()) {
                        sentRequestRecyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading receiver meeting requests", e);
                    swipeRefreshLayout.setRefreshing(false);
                    handleNoRequests();
                });

        // Get requests where user is the sender
        db.collection("meetingRequests")
                .whereEqualTo("senderId", user.getUid())
                .get()
                .addOnSuccessListener(senderDocuments -> {
                    for (DocumentSnapshot document : senderDocuments) {
                        MeetingRequest request = document.toObject(MeetingRequest.class);
                        if (request != null) {
                            requestList.add(request);
                        }
                    }

                    // Sort by creation time (newest first)
                    Collections.sort(requestList, (r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

                    requestAdapter.notifyDataSetChanged();

                    if (requestList.isEmpty()) {
                        handleNoRequests();
                    } else {
                        emptyRequestsView.setVisibility(View.GONE);
                        requestRecyclerView.setVisibility(View.VISIBLE);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading sender meeting requests", e);
                    swipeRefreshLayout.setRefreshing(false);
                    handleNoRequests();
                });
    }

//    private void loadRequests() {
//        swipeRefreshLayout.setRefreshing(true);
//
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user == null) {
//            handleNoRequests();
//            return;
//        }
//
//        // Clear previous list
//        requestList.clear();
//        requestAdapter.notifyDataSetChanged();
//        // Query where user is the receiver
//        db.collection("meetingRequests")
//                .whereEqualTo("receiverId", user.getUid())
//                .get()
//                .addOnSuccessListener(queryDocuments -> {
//                    for (DocumentSnapshot document : queryDocuments) {
//                        MeetingRequest request = document.toObject(MeetingRequest.class);
//                        if (request != null) {
//                            requestList.add(request);
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error loading receiver meeting requests", e);
//                    swipeRefreshLayout.setRefreshing(false);
//                    handleNoRequests();
//                });
//    }
    
    private void handleNoRequests() {
        if (emptyRequestsView != null && requestRecyclerView != null) {
            emptyRequestsView.setVisibility(View.VISIBLE);
            requestRecyclerView.setVisibility(View.GONE);
        }
    }
    
    private void handleFailure(String message, Exception e) {
        Log.e(TAG, message, e);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // Stop refresh animation if running
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
        // Update empty state in case of failure
        handleNoRequests();
    }

    private void setupLogoutButton() {
        ImageButton logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                // Sign out the user
                mAuth.signOut();
                
                // Redirect to login screen
                Intent intent = new Intent(NotificationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}