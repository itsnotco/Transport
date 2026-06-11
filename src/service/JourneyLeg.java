package service;

import model.Station;
import model.VehicleType;

public class JourneyLeg {

    private final String vehicleId;
    private final VehicleType type;
    private final Station from;
    private final Station to;
    private final int boardTime;
    private final int alightTime;

    public JourneyLeg(String vehicleId, VehicleType type, Station from, Station to, int boardTime, int alightTime) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.from = from;
        this.to = to;
        this.boardTime = boardTime;
        this.alightTime = alightTime;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public VehicleType getType() {
        return type;
    }

    public Station getFrom() {
        return from;
    }

    public Station getTo() {
        return to;
    }

    public int getBoardTime() {
        return boardTime;
    }

    public int getAlightTime() {
        return alightTime;
    }
}