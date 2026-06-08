package org.example.model;

public class Tram extends Vehicle {
    public Tram(String id, int capacity) {
        super(id, capacity);
    }

    public VehicleType getType() {
        return VehicleType.TRAM;
    }

    public double getSpeed() {
        return 0.5;
    }
}