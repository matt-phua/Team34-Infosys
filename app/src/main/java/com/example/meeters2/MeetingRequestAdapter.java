package com.example.meeters2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeetingRequestAdapter extends RecyclerView.Adapter<MeetingRequestAdapter.MeetingRequestViewHolder> {

    private List<MeetingRequest> meetingRequests;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public MeetingRequestAdapter(Context context, List<MeetingRequest> meetingRequests) {
        this.context = context;
        this.meetingRequests = meetingRequests;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
    }

    @NonNull
    @Override
    public MeetingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meeting_request, parent, false);
        return new MeetingRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingRequestViewHolder holder, int position) {
        MeetingRequest request = meetingRequests.get(position);
        boolean isCurrentUserSender = request.getSenderId().equals(currentUserId);

        // Set name based on whether current user is sender or receiver
        String displayName = isCurrentUserSender ? request.getReceiverName() : request.getSenderName();
        holder.nameTextView.setText(displayName);
        
        // Set message
        holder.messageTextView.setText(request.getMessage());
        
        // Handle UI based on request status
        switch (request.getStatus()) {
            case "pending":
                setupPendingUI(holder, request, isCurrentUserSender);
                break;
            case "accepted":
                setupAcceptedUI(holder, request);
                break;
            case "declined":
                // If declined and viewer is the receiver, don't show it at all
                if (!isCurrentUserSender) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                } else {
                    holder.statusTextView.setText("Declined");
                    holder.actionButtonsLayout.setVisibility(View.GONE);
                    holder.replyLayout.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void setupPendingUI(MeetingRequestViewHolder holder, MeetingRequest request, boolean isCurrentUserSender) {
        if (isCurrentUserSender) {
            // Sender sees "Pending" status
            holder.statusTextView.setText("Pending");
            holder.actionButtonsLayout.setVisibility(View.GONE);
            holder.replyLayout.setVisibility(View.GONE);
        } else {
            // Receiver sees accept/decline buttons
            holder.statusTextView.setText("New Request");
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
            holder.replyLayout.setVisibility(View.GONE);
            
            // Set up accept button
            holder.acceptButton.setOnClickListener(v -> {
                request.setStatus("accepted");
                updateRequestInFirestore(request);
                notifyDataSetChanged();
            });
            
            // Set up decline button
            holder.declineButton.setOnClickListener(v -> {
                request.setStatus("declined");
                updateRequestInFirestore(request);
                notifyDataSetChanged();
            });
        }
    }

    private void setupAcceptedUI(MeetingRequestViewHolder holder, MeetingRequest request) {
        holder.statusTextView.setText("Accepted");
        holder.actionButtonsLayout.setVisibility(View.GONE);
        holder.replyLayout.setVisibility(View.VISIBLE);
        
        // Set up reply button
        holder.sendReplyButton.setOnClickListener(v -> {
            String replyMessage = holder.replyEditText.getText().toString().trim();
            if (!replyMessage.isEmpty()) {
                sendMessage(request, replyMessage);
                holder.replyEditText.setText("");
                Toast.makeText(context, "Message sent", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRequestInFirestore(MeetingRequest request) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", request.getStatus());
        updates.put("updatedAt", new Date());
        
        db.collection("meetingRequests").document(request.getId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Request " + request.getStatus(), Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Error updating request", Toast.LENGTH_SHORT).show();
            });
    }

    private void sendMessage(MeetingRequest request, String message) {
        // Create a chat message in Firestore
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("senderId", currentUserId);
        chatMessage.put("receiverId", currentUserId.equals(request.getSenderId()) ? request.getReceiverId() : request.getSenderId());
        chatMessage.put("message", message);
        chatMessage.put("timestamp", new Date());
        chatMessage.put("requestId", request.getId());
        
        db.collection("messages").add(chatMessage)
            .addOnSuccessListener(documentReference -> {
                // Message sent successfully
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public int getItemCount() {
        return meetingRequests.size();
    }

    public void updateData(List<MeetingRequest> newRequests) {
        this.meetingRequests = newRequests;
        notifyDataSetChanged();
    }

    public static class MeetingRequestViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView messageTextView;
        TextView statusTextView;
        LinearLayout actionButtonsLayout;
        Button acceptButton;
        Button declineButton;
        LinearLayout replyLayout;
        EditText replyEditText;
        MaterialButton sendReplyButton;

        public MeetingRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.requestNameTextView);
            messageTextView = itemView.findViewById(R.id.requestMessageTextView);
            statusTextView = itemView.findViewById(R.id.requestStatusTextView);
            actionButtonsLayout = itemView.findViewById(R.id.requestActionsLayout);
            acceptButton = itemView.findViewById(R.id.acceptRequestButton);
            declineButton = itemView.findViewById(R.id.declineRequestButton);
            replyLayout = itemView.findViewById(R.id.replyLayout);
            replyEditText = itemView.findViewById(R.id.replyEditText);
            sendReplyButton = itemView.findViewById(R.id.sendReplyButton);
        }
    }
}
