package com.example.meeters2;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

public class BaseActivity extends AppCompatActivity {
    protected FirebaseAuth mAuth;
    protected FirebaseFirestore db;
    protected TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        welcomeText = findViewById(R.id.welcomeText);
        if (welcomeText != null) {
            loadUserName();
        }
    }

    protected void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && welcomeText != null) {
            db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        if (firstName != null) {
                            welcomeText.setText("Hello, " + firstName);
                        } else {
                            // Fallback to email if name not available
                            String email = user.getEmail();
                            String displayName = email != null ? email.split("@")[0] : "User";
                            welcomeText.setText("Hello, " + displayName);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback if Firestore query fails
                    String email = user.getEmail();
                    String displayName = email != null ? email.split("@")[0] : "User";
                    welcomeText.setText("Hello, " + displayName);
                });
        }
    }
} 