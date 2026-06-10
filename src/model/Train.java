package model;

public class Train extends Vehicle {

    public Train(String id, int capacity) {
        super(id, capacity);
    }

    @Override
    public VehicleType getType() {
        return VehicleType.TRAIN;
    }

    @Override
    public double getSpeed() {
        return 2.5;
    }
}