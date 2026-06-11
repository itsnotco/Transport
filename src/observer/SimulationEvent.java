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

    public SimulationEvent(Kind kind, Vehicle vehicle, Station from, Station to) {
        this.kind = kind;
        this.vehicle = vehicle;
        this.from = from;
        this.to = to;
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
}