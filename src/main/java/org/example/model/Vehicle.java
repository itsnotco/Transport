package org.example.model;

import java.util.ArrayList;

public abstract class Vehicle {

    public enum State {
        DOCKED,
        IN_TRANSIT,
        OUT_OF_SERVICE
    }

    private String id;
    private int capacity;
    private ArrayList<Passenger> onboard;
    private Station currentStation;
    private State state;

    public Vehicle(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.onboard = new ArrayList<Passenger>();
        this.currentStation = null;
        this.state = State.DOCKED;
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

    public boolean isInTransit() {
        return state == State.IN_TRANSIT;
    }

    public boolean isOperational() {
        return state != State.OUT_OF_SERVICE;
    }

    public void setCurrentStation(Station s) {
        this.currentStation = s;
        this.state = State.DOCKED;
    }

    public void setInTransit() {
        this.state = State.IN_TRANSIT;
        this.currentStation = null;
    }

    public void setOutOfService() {
        this.state = State.OUT_OF_SERVICE;
    }

    public String getId()                        {
        return id;
    }
    public int getCapacity()                     { return capacity; }
    public ArrayList<Passenger> getOnboard()     { return onboard; }
    public Station getCurrentStation()           { return currentStation; }
    public State getState()                      { return state; }
}