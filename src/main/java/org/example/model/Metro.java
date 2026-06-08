package org.example.model;

public class Metro extends Vehicle {
    public Metro(String id, int capacity) {
        super(id, capacity);
    }

    public VehicleType getType() {
        return VehicleType.METRO;
    }

    public double getSpeed() {
        return 1.0;
    }
}