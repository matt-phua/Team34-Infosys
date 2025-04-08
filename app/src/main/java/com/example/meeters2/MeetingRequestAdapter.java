package com.example.meeters2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MeetingRequestAdapter extends RecyclerView.Adapter<MeetingRequestAdapter.ViewHolder> {

    private final List<MeetingRequest> requestList;
    private final Context context;
    private final String currentUserId;

    public MeetingRequestAdapter(Context context, List<MeetingRequest> requestList, String currentUserId) {
        this.context = context;
        this.requestList = requestList;
        this.currentUserId = currentUserId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderName, message;
        Button acceptButton, rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.textSenderName);
            message = itemView.findViewById(R.id.textRequestMessage);
            acceptButton = itemView.findViewById(R.id.buttonAccept);
            rejectButton = itemView.findViewById(R.id.buttonReject);
        }
    }

    @NonNull
    @Override
    public MeetingRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meeting_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingRequestAdapter.ViewHolder holder, int position) {
        MeetingRequest request = requestList.get(position);

        holder.senderName.setText(request.getSenderName());
        holder.message.setText(request.getMessage());

        holder.acceptButton.setOnClickListener(v -> showResponseDialog(request, "accepted"));
        holder.rejectButton.setOnClickListener(v -> showResponseDialog(request, "rejected"));
    }

    private void showResponseDialog(MeetingRequest request, String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(status.equals("accepted") ? "Accept Request" : "Reject Request");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_response_message, null);
        EditText responseInput = dialogView.findViewById(R.id.responseMessageEditText);
        builder.setView(dialogView);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String responseMessage = responseInput.getText().toString().trim();
            if (responseMessage.isEmpty()) {
                Toast.makeText(context, "Response cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firestore
            Map<String, Object> update = new HashMap<>();
            update.put("status", status);
            update.put("responseMessage", responseMessage);
            update.put("timestamp", FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document(currentUserId)
                    .collection("incoming")
                    .document(request.getSenderId())
                    .update(update)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Request " + status, Toast.LENGTH_SHORT).show()

                    );
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }
}
