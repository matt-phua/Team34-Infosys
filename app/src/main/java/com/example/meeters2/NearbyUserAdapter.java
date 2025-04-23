package com.example.meeters2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class NearbyUserAdapter extends RecyclerView.Adapter<NearbyUserAdapter.ViewHolder> {

    private List<NearbyUser> userList;
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    // Sample interests for demo
    private String[] sampleInterests = {"Coffee", "Networking", "Tech", "Startups", "Finance", "Marketing", "Design"};
    private Random random = new Random();

    public NearbyUserAdapter(List<NearbyUser> userList) {
        this.userList = userList;
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_nearby_user, parent, false);
        return new ViewHolder(view);
    }

    // Bind data to the views in the ViewHolder
    // This method is called for each item in the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NearbyUser user = userList.get(position);
        holder.nameTextView.setText(user.getName());
        
        // Set distance if available
        if (user.getDistance() > 0) {
            String distanceText = String.format("%.1f km away", user.getDistance());
            holder.distanceTextView.setText(distanceText);
            holder.distanceTextView.setVisibility(View.VISIBLE);
        } else {
            holder.distanceTextView.setVisibility(View.GONE);
        }
        
        // Set a random interest for demo purposes
        // In a real app, you would get this from the user's profile
        if (holder.interestChip != null) {
            // Show a random interest for 70% of users
            if (random.nextFloat() < 0.7f) {
                String interest = sampleInterests[random.nextInt(sampleInterests.length)];
                holder.interestChip.setText(interest);
                holder.interestChip.setVisibility(View.VISIBLE);
            } else {
                holder.interestChip.setVisibility(View.GONE);
            }
        }
        
        holder.connectButton.setOnClickListener(v -> {
            showMeetingRequestDialog(user);
        });
    }

    // Show a dialog to send a meeting request to the selected user
    // This method is called when the user clicks the "Connect" button on a nearby user
    private void showMeetingRequestDialog(NearbyUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Send Meeting Request");
        
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_meeting_request, null);
        final EditText messageEditText = dialogView.findViewById(R.id.messageEditText);
        builder.setView(dialogView);
        
        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = messageEditText.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMeetingRequest(user, message);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Send a meeting request to the selected user
    // This method is called when the user clicks "Send" in the meeting request dialog
    private void sendMeetingRequest(NearbyUser receiver, String message) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        // Get current user's first and last name from Firestore
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    
                    if (firstName != null && lastName != null) {
                        String senderName = firstName + " " + lastName;
                        createMeetingRequest(currentUserId, senderName, receiver.getId(), receiver.getName(), message);
                    } else {
                        Toast.makeText(context, "Could not get your profile information", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void createMeetingRequest(String senderId, String senderName, String receiverId, String receiverName, String message) {
        // Generate a unique ID for the meeting request
        String requestId = UUID.randomUUID().toString();
        
        // Create the meeting request object
        MeetingRequest request = new MeetingRequest(
            requestId,
            senderId,
            senderName,
            receiverId,
            receiverName,
            message,
            "pending",
            new Date()
        );
        
        // Add to Firestore
        db.collection("meetingRequests").document(requestId)
            .set(request)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Meeting request sent!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView distanceTextView;
        MaterialButton connectButton;
        Chip interestChip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.userNameTextView);
            distanceTextView = itemView.findViewById(R.id.userDistanceTextView);
            connectButton = itemView.findViewById(R.id.connectButton);
            interestChip = itemView.findViewById(R.id.userInterestChip);
        }
    }
}