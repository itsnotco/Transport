package model;

public class Connection {

    private Station stationA;
    private Station stationB;
    private double distance;
    private boolean blocked = false;

    public Connection(Station stationA, Station stationB, double distance) {
        this.stationA = stationA;
        this.stationB = stationB;
        this.distance = distance;
    }

    public boolean involves(Station s) {
        return stationA == s || stationB == s;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Station getStationA() {
        return stationA;
    }

    public Station getStationB() {
        return stationB;
    }

    public double getDistance() {
        return distance;
    }
}