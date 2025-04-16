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

public class ProfileActivity extends BaseActivity {
    private TextView nameText;
    private TextView emailText;
    private EditText aboutMeText;
    private ImageButton editAboutButton;
    private BottomNavigationView bottomNavigation;
    private boolean isEditing = false;


    private void initializeViews() {
        //nameText = findViewById(R.id.nameText);
        //profileImage = findViewById(R.id.profileImage);
        logoutButton = findViewById(R.id.logoutButton);
        notificationButton = findViewById(R.id.notificationButton);
//      pcomingEventsRecyclerView = findViewById(R.id.upcomingEventsRecyclerView);
//      suggestedMatchesRecyclerView = findViewById(R.id.suggestedMatchesRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // Find the ToggleButton
        ToggleButton meetingsToggle = findViewById(R.id.meetings_toggle);
        meetingsToggle.setTextColor(Color.RED);

        meetingsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                meetingsToggle.setTextColor(Color.GREEN);
                updateMeetingStatus(true);
            } else {
                meetingsToggle.setTextColor(Color.RED);
                updateMeetingStatus(false);
            }
        });

        initializeViews();
        setupToolbar();
        setupBottomNavigation();
        setupEditButton();
        setupLogoutButton();
        loadUserData();
    }

    private void initializeViews() {
        nameText = findViewById(R.id.profile_name);
        emailText = findViewById(R.id.profile_email);
        aboutMeText = findViewById(R.id.about_me_text);
        editAboutButton = findViewById(R.id.edit_about_button);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupEditButton() {
        if (editAboutButton == null || aboutMeText == null) return;
        
        editAboutButton.setOnClickListener(v -> {
            isEditing = !isEditing;
            aboutMeText.setEnabled(isEditing);
            if (isEditing) {
                editAboutButton.setImageResource(R.drawable.ic_save);
                aboutMeText.requestFocus();
            } else {
                editAboutButton.setImageResource(R.drawable.ic_edit);
                saveAboutMe();
            }
        });
    }

    private void saveAboutMe() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("aboutMe", aboutMeText.getText().toString());
            
            db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Set email
            emailText.setText(user.getEmail());

            // Load user data from Firestore
            db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String aboutMe = documentSnapshot.getString("aboutMe");
                        Boolean isMeeting = documentSnapshot.getBoolean("isMeeting");

                        if (firstName != null && lastName != null) {
                            nameText.setText(firstName + " " + lastName);
                        } else {
                            nameText.setText("User");
                        }

                        if (aboutMe != null) {
                            aboutMeText.setText(aboutMe);
                        }
                        
                        // Set the default meeting status to true if not set previously
                        ToggleButton meetingsToggle = findViewById(R.id.meetings_toggle);
                        if (isMeeting != null) {
                            meetingsToggle.setChecked(isMeeting);
                            meetingsToggle.setTextColor(isMeeting ? Color.GREEN : Color.RED);
                        } else {
                            // Default to meeting (true) if not set
                            meetingsToggle.setChecked(true);
                            meetingsToggle.setTextColor(Color.GREEN);
                            updateMeetingStatus(true);
                        }
                    }
                });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;
        
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_events) {
                Intent intent = new Intent(ProfileActivity.this, NotificationActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                return true;
            }
            return false;
        });
        
        // Set the profile item as selected
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
    }

    private void updateMeetingStatus(boolean isMeeting) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("isMeeting", isMeeting);
            
            db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Meeting status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update meeting status", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void setupLogoutButton() {
        ImageButton logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                // Sign out the user
                mAuth.signOut();
                
                // Redirect to login screen
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            });
        }

    }
}
