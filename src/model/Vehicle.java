package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Vehicle {

    private String id;
    private int capacity;
    private VehicleState state;

    private List<Station> route;
    private int routeIndex;
    private double timeUntilNextStation;
    private double timeParked;

    public Vehicle(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.state = VehicleState.DOCKED;
        this.route = new ArrayList<>();
        this.routeIndex = 0;
        this.timeUntilNextStation = 0.0;
        this.timeParked = 0.0;
    }

    public abstract VehicleType getType();

    public abstract double getSpeed();

    public void setRoute(List<Station> route) {
        this.route = route;
        this.routeIndex = 0;
        this.state = VehicleState.DOCKED;
        this.timeUntilNextStation = 0.0;
        this.timeParked = 0.0;
    }

    public Station getCurrentStation() {
        if (state == VehicleState.DOCKED) {
            return route.get(routeIndex);
        }
        return null;
    }

    // La prochaine station est toujours celle juste apres routeIndex
    public Station getNextStation() {
        if (routeIndex + 1 < route.size()) {
            return route.get(routeIndex + 1);
        }
        return null;
    }

    public boolean isParked() {
        return state == VehicleState.DOCKED;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public VehicleState getState() {
        return state;
    }

    public List<Station> getRoute() {
        return route;
    }

    public int getRouteIndex() {
        return routeIndex;
    }

    public double getTimeUntilNextStation() {
        return timeUntilNextStation;
    }

    public void setRouteIndex(int index) {
        this.routeIndex = index;
    }

    public void setState(VehicleState state) {
        this.state = state;
    }

    public void setTimeUntilNextStation(double time) {
        this.timeUntilNextStation = time;
    }

    public double getTimeParked() {
        return timeParked;
    }

    public void setTimeParked(double time) {
        this.timeParked = time;
    }
}