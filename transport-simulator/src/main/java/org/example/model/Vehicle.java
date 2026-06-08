package org.example.model;

import java.util.ArrayList;

public abstract class Vehicle {

    private String id;
    private int capacity;
    private ArrayList<Passenger> onboard;
    private Station currentStation;
    private boolean inTransit;

    public Vehicle(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.onboard = new ArrayList<Passenger>();
        this.currentStation = null;
        this.inTransit = false;
    }

    public abstract VehicleType getType();
    public abstract double getSpeed();

    public void boardPassenger(Passenger p) {
        if (onboard.size() < capacity) {
            onboard.add(p);
        }
    }

    public void alightPassenger(Passenger p) {
        onboard.remove(p);
    }

    public boolean isFull() {
        return onboard.size() >= capacity;
    }

    public String getId() {
        return id;
    }
    public int getCapacity() {
        return capacity;
    }
    public ArrayList<Passenger> getOnboard() {
        return onboard;
    }
    public Station getCurrentStation() {
        return currentStation;
    }
    public boolean isInTransit() {
        return inTransit;
    }

    public void setCurrentStation(Station s) {
        this.currentStation = s;
        this.inTransit = false;
    }

    public void setInTransit() {
        this.inTransit = true;
        this.currentStation = null;
    }
}