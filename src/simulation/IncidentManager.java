package simulation;

import model.Connection;
import model.Station;
import model.Vehicle;
import model.VehicleState;
import network.NetworkGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IncidentManager {

    private static final double SPAWN_PROBABILITY = 0.02;
    private static final int MIN_DURATION = 15;
    private static final int MAX_DURATION = 40;

    private final NetworkGraph graph;
    private final List<Vehicle> vehicles;
    private final Random rnd = new Random();

    private final List<Incident> active = new ArrayList<>();

    public IncidentManager(NetworkGraph graph, List<Vehicle> vehicles) {
        this.graph = graph;
        this.vehicles = vehicles;
    }

    public void tick(int now) {
        List<Incident> ended = new ArrayList<>();
        for (Incident i : active) {
            if (now >= i.endTime) {
                i.resolve();
                System.out.println("  [INCIDENT RESOLU] " + i.describe());
                ended.add(i);
            }
        }
        active.removeAll(ended);

        if (rnd.nextDouble() < SPAWN_PROBABILITY) {
            Incident i = createRandom(now);
            if (i != null) {
                i.apply();
                active.add(i);
                System.out.println("  [INCIDENT] " + i.describe());
            }
        }
    }

    private Incident createRandom(int now) {
        int duration = MIN_DURATION + rnd.nextInt(MAX_DURATION - MIN_DURATION + 1);
        int end = now + duration;
        int type = rnd.nextInt(3);

        switch (type) {
            case 0: return makeVehicleIncident(end);
            case 1: return makeConnectionIncident(end);
            default: return makeStationIncident(end);
        }
    }

    private Incident makeVehicleIncident(int end) {
        List<Vehicle> ok = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (v.getState() != VehicleState.OUT_OF_SERVICE) ok.add(v);
        }
        if (ok.isEmpty()) return null;
        Vehicle v = ok.get(rnd.nextInt(ok.size()));
        return new VehicleIncident(v, end);
    }

    private Incident makeConnectionIncident(int end) {
        List<Connection> ok = new ArrayList<>();
        for (Connection c : graph.getConnections()) {
            if (!c.isBlocked()) ok.add(c);
        }
        if (ok.isEmpty()) return null;
        Connection c = ok.get(rnd.nextInt(ok.size()));
        return new ConnectionIncident(c, end);
    }

    private Incident makeStationIncident(int end) {
        List<Station> ok = new ArrayList<>();
        for (Station s : graph.getStations()) {
            if (!s.isClosed()) ok.add(s);
        }
        if (ok.isEmpty()) return null;
        Station s = ok.get(rnd.nextInt(ok.size()));
        return new StationIncident(s, end);
    }

    public List<Incident> getActive() {
        return active;
    }

    public abstract static class Incident {
        final int endTime;
        Incident(int endTime) { this.endTime = endTime; }
        abstract void apply();
        abstract void resolve();
        public abstract String describe();
        public int remaining(int now) {
            return Math.max(0, endTime - now);
        }
    }

    public static class VehicleIncident extends Incident {
        private final Vehicle vehicle;
        VehicleIncident(Vehicle v, int end) { super(end); this.vehicle = v; }
        void apply() {
            vehicle.setState(VehicleState.OUT_OF_SERVICE);
        }
        void resolve() { vehicle.setState(VehicleState.DOCKED); }
        public String describe() { return "Vehicule " + vehicle.getId() + " hors service"; }
    }

    public static class ConnectionIncident extends Incident {
        private final Connection connection;
        ConnectionIncident(Connection c, int end) { super(end); this.connection = c; }
        void apply() { connection.setBlocked(true); }
        void resolve() { connection.setBlocked(false); }
        public String describe() {
            return "Connexion " + connection.getStationA().getName() + " <-> " + connection.getStationB().getName() + " bloquee";
        }
    }

    public static class StationIncident extends Incident {
        private final Station station;
        StationIncident(Station s, int end) { super(end); this.station = s; }
        void apply() { station.setClosed(true); }
        void resolve() { station.setClosed(false); }
        public String describe() { return "Gare " + station.getName() + " fermee"; }
    }
}