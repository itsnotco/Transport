package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Station {

    private String id;
    private String name;
    private int capacity;
    private List<VehicleType> supportedTypes;

    private final List<Passenger> waiting = new ArrayList<>();

    public Station(String id, String name, int capacity, VehicleType... supportedTypes) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.supportedTypes = Arrays.asList(supportedTypes);
    }

    public boolean supports(VehicleType type) {
        return supportedTypes.contains(type);
    }

    public void addWaiting(Passenger p) {
        waiting.add(p);
    }

    public void removeWaiting(Passenger p) {
        waiting.remove(p);
    }

    public List<Passenger> getWaiting() {
        return waiting;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<VehicleType> getSupportedTypes() {
        return supportedTypes;
    }
}