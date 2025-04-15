package com.example.meeters2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity {
    private TextView nameText;
    private RecyclerView requestRecyclerView;
    private MeetingRequestAdapter requestAdapter;
    private List<MeetingRequest> requestList = new ArrayList<>();
    private BottomNavigationView bottomNavigation;      // Bottom navigation bar
    private View emptyRequestsView;
    private ImageButton homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupBottomNavigation();
        setupLogoutButton();
        
        // Load meeting requests
        loadMeetingRequests();
    }

    private void initializeViews() {
        //nameText = findViewById(R.id.nameText);
//      profileImage = findViewById(R.id.profileImage);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        requestRecyclerView = findViewById(R.id.requestRecyclerView);
        emptyRequestsView = findViewById(R.id.emptyRequestsView);
        homeButton = findViewById(R.id.homeButton);
        
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    /**
     * Set up the toolbar with custom styling
     */
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    //String displayName = email != null ? email.split("@")[0] : "User";
    //nameText.setText(displayName);

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;
        
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_events) {
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        
        // Set the events item as selected
        bottomNavigation.setSelectedItemId(R.id.navigation_events);
    }

    private void setupRecyclerView() {
        if (requestRecyclerView == null) return;
        
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestAdapter = new MeetingRequestAdapter(this, requestList);
        requestRecyclerView.setAdapter(requestAdapter);
    }

    private void loadMeetingRequests() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        // Load meeting requests where user is the receiver
        db.collection("meetingRequests")
            .whereEqualTo("receiverId", currentUserId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                requestList.clear();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    MeetingRequest request = doc.toObject(MeetingRequest.class);
                    if (request != null) {
                        requestList.add(request);
                    }
                }
                
                // Also load requests where user is the sender
                db.collection("meetingRequests")
                    .whereEqualTo("senderId", currentUserId)
                    .get()
                    .addOnSuccessListener(senderSnapshot -> {
                        for (DocumentSnapshot doc : senderSnapshot.getDocuments()) {
                            MeetingRequest request = doc.toObject(MeetingRequest.class);
                            if (request != null) {
                                requestList.add(request);
                            }
                        }
                        requestAdapter.notifyDataSetChanged();
                        
                        // Show empty view if no requests
                        if (emptyRequestsView != null) {
                            if (requestList.isEmpty()) {
                                emptyRequestsView.setVisibility(View.VISIBLE);
                                requestRecyclerView.setVisibility(View.GONE);
                            } else {
                                emptyRequestsView.setVisibility(View.GONE);
                                requestRecyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load sent requests", Toast.LENGTH_SHORT).show();
                        Log.e("NotificationActivity", "Error loading sent requests", e);
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load received requests", Toast.LENGTH_SHORT).show();
                Log.e("NotificationActivity", "Error loading received requests", e);
            });
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