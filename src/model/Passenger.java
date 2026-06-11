package model;

import service.JourneyLeg;

import java.util.ArrayList;
import java.util.List;

public class Passenger {

    private final String id;
    private Station origin;
    private Station destination;

    private PassengerState state;
    private Station currentStation;
    private List<JourneyLeg> plan;
    private int legIndex;

    private boolean tracked;

    public Passenger(String id, Station origin, Station destination) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.state = PassengerState.WAITING;
        this.currentStation = origin;
        this.plan = new ArrayList<>();
        this.legIndex = 0;
        this.tracked = false;
    }

    public void setPlan(List<JourneyLeg> plan) {
        this.plan = plan;
        this.legIndex = 0;
    }

    public JourneyLeg getCurrentLeg() {
        if (legIndex < plan.size()) {
            return plan.get(legIndex);
        }
        return null;
    }

    public void advanceLeg() {
        legIndex++;
    }

    public boolean hasMoreLegs() {
        return legIndex < plan.size();
    }

    public List<JourneyLeg> getPlan() {
        return plan;
    }

    public PassengerState getState() {
        return state;
    }

    public void setState(PassengerState state) {
        this.state = state;
    }

    public Station getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(Station station) {
        this.currentStation = station;
    }

    public boolean isTracked() {
        return tracked;
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }

    public String getId() {
        return id;
    }

    public Station getOrigin() {
        return origin;
    }

    public void setOrigin(Station origin) {
        this.origin = origin;
    }

    public Station getDestination() {
        return destination;
    }

    public void setDestination(Station destination) {
        this.destination = destination;
    }
}