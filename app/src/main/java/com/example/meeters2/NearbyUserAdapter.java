package com.example.meeters2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.Toast;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
public class NearbyUserAdapter extends RecyclerView.Adapter<NearbyUserAdapter.ViewHolder> {

    private final List<NearbyUser> userList;

    public NearbyUserAdapter(List<NearbyUser> userList) {
        this.userList = userList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNearbyName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNearbyName = itemView.findViewById(R.id.textNearbyName);
        }
    }

    @NonNull
    @Override
    public NearbyUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nearby_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyUserAdapter.ViewHolder holder, int position) {
        NearbyUser user = userList.get(position);
        holder.textNearbyName.setText(user.getName());

        // Handle click to send request
        holder.itemView.setOnClickListener(v -> {
            String receiverId = user.getId();
            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> request = new HashMap<>();
            request.put("timestamp", FieldValue.serverTimestamp());
            request.put("status", "pending");

            FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document(receiverId)
                    .collection("incoming")
                    .document(senderId)
                    .set(request)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Meeting request sent!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(v.getContext(), "Failed to send request.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

