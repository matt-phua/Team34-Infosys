package com.example.meeters2;

import java.util.Date;

public class MeetingRequest {
    private String id;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String message;
    private String status; // "pending", "accepted", "declined"
    private Date createdAt;
    private Date updatedAt;

    // Empty constructor for Firestore
    public MeetingRequest() {
    }

    // Constructor for creating a new meeting request and initializing the createdAt and updatedAt fields
    public MeetingRequest(String id, String senderId, String senderName, String receiverId, 
                        String receiverName, String message, String status, Date createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    // This field is set when the request is created and should not be modified to track when it is created
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}


