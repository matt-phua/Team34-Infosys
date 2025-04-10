package com.example.meeters2;

public class NearbyUser {
    private final String id;
    private final String name;

    public NearbyUser(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
