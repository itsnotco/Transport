package model;

public class Tram extends Vehicle {

    public Tram(String id, int capacity) {
        super(id, capacity);
    }
    @Override
    public VehicleType getType() {
        return VehicleType.TRAM;
    }

    @Override
    public double getSpeed() {
        return 0.5;
    }
}