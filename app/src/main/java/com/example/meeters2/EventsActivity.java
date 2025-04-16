package com.example.meeters2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventsActivity extends BaseActivity {

    private RecyclerView requestRecyclerView;
    private MeetingRequestAdapter requestAdapter;
    private List<MeetingRequest> requestList = new ArrayList<>();

    private ImageButton logoutButton, notificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();

        // Setup bottom nav bar
        setupBottomNavigation();

        // Set click listeners for top bar buttons
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(EventsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        notificationButton.setOnClickListener(v -> {
            startActivity(new Intent(EventsActivity.this, NotificationActivity.class));
            finish();
        });

        // Setup RecyclerView
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestAdapter = new MeetingRequestAdapter(this, requestList);
        requestRecyclerView.setAdapter(requestAdapter);

        // Load requests
        loadRequests();
    }

    private void loadRequests() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Load requests from meetingRequests collection where the current user is either sender or receiver
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
                    
                    // Also get the requests where user is the sender
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
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to load sent requests", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load received requests", Toast.LENGTH_SHORT).show();
                });
    }

    private void initializeViews() {
        logoutButton = findViewById(R.id.logoutButton);
        notificationButton = findViewById(R.id.notificationsButton);
        requestRecyclerView = findViewById(R.id.requestRecyclerView);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(EventsActivity.this, MainActivity.class));
                finish();
                return true;
            }  else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(EventsActivity.this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }
}
