package com.example.choresappv2;

public class ChoreItem {
    private int id;
    private String name;
    private int points;

    public ChoreItem(int id, String name, int points) {
        this.id = id;
        this.name = name;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }
}
