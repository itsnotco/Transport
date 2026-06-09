package model;

import java.util.List;

public class Passenger {

    private String id;
    private Station origin;
    private Station destination;
    private VehicleType preferredType;
    private List<Station> route;

    public Passenger(String id, Station origin, Station destination, VehicleType preferredType) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.preferredType = preferredType;
        this.route = null;
    }

    public void setRoute(List<Station> route) {
        this.route = route;
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

    public VehicleType getPreferredType() {
        return preferredType;
    }

    public List<Station> getRoute() {
        return route;
    }
}