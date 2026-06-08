package org.example.model;

import java.util.ArrayList;

public class Station {

    private String id;
    private String name;
    private int capacity;
    private VehicleType[] supportedTypes;
    private ArrayList<Passenger> waitingPassengers;

    public Station(String id, String name, int capacity, VehicleType[] supportedTypes) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.supportedTypes = supportedTypes;
        this.waitingPassengers = new ArrayList<Passenger>();
    }

    public boolean supports(VehicleType type) {
        for (VehicleType t : supportedTypes) {
            if (t == type) return true;
        }
        return false;
    }

    public void addPassenger(Passenger p) {
        waitingPassengers.add(p);
    }

    public void removePassenger(Passenger p) {
        waitingPassengers.remove(p);
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
    public ArrayList<Passenger> getWaitingPassengers() {
        return waitingPassengers;
    }
}