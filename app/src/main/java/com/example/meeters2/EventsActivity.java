package com.example.meeters2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventsActivity extends AppCompatActivity {

    private RecyclerView requestRecyclerView;
    private MeetingRequestAdapter requestAdapter;
    private List<MeetingRequest> requestList = new ArrayList<>();

    private ImageButton logoutButton, notificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events); // layout with top bar and nav bar

        // Initialize views
        initializeViews();

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
        });

        // Setup RecyclerView
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestAdapter = new MeetingRequestAdapter(this, requestList, currentUserId);
        requestRecyclerView.setAdapter(requestAdapter);

        // Load requests
        loadRequests();
    }


    private void loadRequests() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(currentUserId)
                .collection("incoming")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    requestList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String senderId = doc.getId();
                        String message = doc.getString("message");
                        String senderName = doc.getString("senderName");

                        MeetingRequest request = new MeetingRequest(senderId, senderName, message);
                        request.setSenderName(senderName);
                        requestList.add(request);
                    }
                    requestAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                });

    }

    private void initializeViews() {
        logoutButton = findViewById(R.id.logoutButton);
        notificationButton = findViewById(R.id.notificationsButton);
        requestRecyclerView = findViewById(R.id.requestRecyclerView);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_events); // highlight Events tab

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(EventsActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.navigation_events) {
                return true; // already on Events
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(EventsActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }


}
