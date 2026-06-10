package observer;

import model.Station;
import model.Vehicle;

public class SimulationEvent {

    public enum Kind {
        DEPARTURE,
        ARRIVAL
    }

    private final Kind kind;
    private final Vehicle vehicle;
    private final Station from;
    private final Station to;
    private final double travelTime;

    public SimulationEvent(Kind kind, Vehicle vehicle, Station from, Station to, double travelTime) {
        this.kind = kind;
        this.vehicle = vehicle;
        this.from = from;
        this.to = to;
        this.travelTime = travelTime;
    }

    public Kind getKind() {
        return kind;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Station getFrom() {
        return from;
    }

    public Station getTo() {
        return to;
    }

    public double getTravelTime() {
        return travelTime;
    }
}