package com.example.meeters2;

public class NearbyUser {
    private String id;
    private String name;
    private double distance; // distance in kilometers

    public NearbyUser(String id, String name) {
        this.id = id;
        this.name = name;
        this.distance = 0;
    }

    public NearbyUser(String id, String name, double distance) {
        this.id = id;
        this.name = name;
        this.distance = distance;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
