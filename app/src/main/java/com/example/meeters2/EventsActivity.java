package com.example.meeters2;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events); // make sure you have this layout file

        requestRecyclerView = findViewById(R.id.requestRecyclerView);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestAdapter = new MeetingRequestAdapter(this, requestList, currentUserId);
        requestRecyclerView.setAdapter(requestAdapter);

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
}
