package com.example.meeters2;


import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;




public class LocationServices {
    //uploads the location into firebase
    public static void uploadLocation(double currentLat, double currentLng) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid(); //gets the current userID
        //handles if user does not exists
        if (userId == null) {
            Log.e("Firestore", "User ID is null");
            return;
        }
        //gets users latitude and longitude
        GeoPoint geoPoint = new GeoPoint(currentLat, currentLng);

        //create a hashmap with location and last updated as keys
        Map<String, Object> data = new HashMap<>();
        data.put("location", geoPoint);
        data.put("lastUpdated", FieldValue.serverTimestamp());
        //store into firebase their locations
        db.collection("users").document(userId)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Location updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating location", e));
    }


}



