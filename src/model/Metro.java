package model;

public class Metro extends Vehicle {

    public Metro(String id, int capacity) {
        super(id, capacity);
    }

    @Override
    public VehicleType getType() {
        return VehicleType.METRO;
    }

    @Override
    public double getSpeed() {
        return 1.0;
    }
}