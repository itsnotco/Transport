package org.example.model;

import java.util.ArrayList;

public class Passenger {

    private String id;
    private Station origin;
    private Station destination;
    private ArrayList<Station> route;
    private int routeIndex;

    public Passenger(String id, Station origin, Station destination) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.route = new ArrayList<Station>();
        this.routeIndex = 0;
    }

    public void setRoute(ArrayList<Station> route) {
        this.route = route;
        this.routeIndex = 0;
    }

    public Station getNextStop() {
        if (routeIndex + 1 < route.size()) {
            return route.get(routeIndex + 1);
        }
        return null;
    }

    public void advanceRoute() {
        routeIndex++;
    }

    public boolean hasArrived() {
        return routeIndex >= route.size() - 1;
    }

    public String getId() {
        return id;
    }

    public Station getOrigin() {
        return origin;
    }

    public Station getDestination() {
        return destination;
    }

    public ArrayList<Station> getRoute() {
        return route;
    }

    public int getRouteIndex() {
        return routeIndex;
    }
}