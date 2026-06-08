package org.example.model;

public class Train extends Vehicle {
    public Train(String id, int capacity) {
        super(id, capacity);
    }

    public VehicleType getType() {
        return VehicleType.TRAIN;
    }

    public double getSpeed() {
        return 2.5;
    }
}