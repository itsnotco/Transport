package simulation;

import model.Station;
import model.Vehicle;
import model.VehicleState;
import network.NetworkGraph;

import java.util.ArrayList;
import java.util.List;

public class VehicleScheduler {

    private static final double PARK_DURATION = 1.0;

    private NetworkGraph graph;

    public VehicleScheduler(NetworkGraph graph) {
        this.graph = graph;
    }

    public List<String> tick(List<Vehicle> vehicles) {
        List<String> events = new ArrayList<>();
        for (Vehicle v : vehicles) {
            tickVehicle(v, events);
        }
        return events;
    }

    private void tickVehicle(Vehicle v, List<String> events) {
        List<Station> route = v.getRoute();

        if (route == null || route.size() < 2) {
            return;
        }

        if (v.isParked()) {
            tickParked(v, route, events);
        } else {
            tickInTransit(v, route, events);
        }
    }

    private void tickParked(Vehicle v, List<Station> route, List<String> events) {
        double parked = v.getTimeParked() + 1.0;

        if (parked < PARK_DURATION) {
            v.setTimeParked(parked);
            return;
        }

        int nextIndex = v.getRouteIndex() + 1;

        if (nextIndex >= route.size()) {
            events.add(v.getId() + " [" + v.getType() + "] terminus : " + route.get(v.getRouteIndex()).getName());
            return;
        }

        Station current   = route.get(v.getRouteIndex());
        Station next      = route.get(nextIndex);
        double distance   = getDistance(current, next);
        double travelTime = distance / v.getSpeed();

        v.setState(VehicleState.IN_TRANSIT);
        v.setTimeParked(0.0);
        v.setTimeUntilNextStation(travelTime - 1.0);
        v.setRouteIndex(nextIndex);

        events.add(v.getId() + " [" + v.getType() + "] "
                + current.getName() + " -> " + next.getName()
                + " (" + String.format("%.1f", travelTime) + " min)");
    }

    private void tickInTransit(Vehicle v, List<Station> route, List<String> events) {
        double remaining = v.getTimeUntilNextStation() - 1.0;

        if (remaining > 0) {
            v.setTimeUntilNextStation(remaining);
            return;
        }

        Station arrived = route.get(v.getRouteIndex());
        v.setState(VehicleState.DOCKED);
        v.setTimeUntilNextStation(0.0);
        v.setTimeParked(0.0);

        events.add(v.getId() + " [" + v.getType() + "] arrive : " + arrived.getName());
    }

    private double getDistance(Station a, Station b) {
        for (var c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) {
                return c.getDistance();
            }
        }
        return 1.0;
    }
}