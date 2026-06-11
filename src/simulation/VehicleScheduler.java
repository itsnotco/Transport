package simulation;

import model.Connection;
import model.Station;
import model.Vehicle;
import model.VehicleState;
import network.NetworkGraph;
import observer.EventBus;
import observer.SimulationEvent;

import java.util.List;

public class VehicleScheduler {

    private static final double PARK_DURATION = 1.0;

    private final NetworkGraph graph;
    private final EventBus bus;

    public VehicleScheduler(NetworkGraph graph, EventBus bus) {
        this.graph = graph;
        this.bus = bus;
    }

    public void tick(List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            tickVehicle(v);
        }
    }

    private void tickVehicle(Vehicle v) {
        List<Station> route = v.getRoute();

        if (route == null || route.size() < 2) {
            return;
        }

        if (v.getState() == VehicleState.OUT_OF_SERVICE) {
            return;
        }

        if (v.isParked()) {
            tickParked(v, route);
        } else {
            tickInTransit(v, route);
        }
    }

    private void tickParked(Vehicle v, List<Station> route) {
        double parked = v.getTimeParked() + 1.0;

        if (parked < PARK_DURATION) {
            v.setTimeParked(parked);
            return;
        }

        int routeSize = route.size();
        int currentIdx = v.getRouteIndex();

        if (currentIdx + 1 >= routeSize) {
            v.setRouteIndex(0);
            currentIdx = 0;
        }

        Station current = route.get(currentIdx);
        int nextIdx = currentIdx + 1;
        Station next = route.get(nextIdx);

        // Don't depart through a blocked connection — wait until it clears
        if (isBlocked(current, next)) {
            v.setTimeParked(0.0);
            return;
        }

        // Skip closed stations, accumulating travel distance through them
        double totalDistance = getDistance(current, next);
        int skipped = 0;
        while (next.isClosed() && skipped < routeSize) {
            skipped++;
            Station prev = next;
            nextIdx++;
            if (nextIdx >= routeSize) nextIdx = 1;
            next = route.get(nextIdx);
            totalDistance += getDistance(prev, next);
        }

        // All stations on route are closed — wait
        if (next.isClosed()) {
            v.setTimeParked(0.0);
            return;
        }

        double travelTime = totalDistance / v.getSpeed();

        // Adjust so tickInTransit increments routeIndex to nextIdx on arrival
        v.setRouteIndex(nextIdx - 1);
        v.setState(VehicleState.IN_TRANSIT);
        v.setTimeParked(0.0);
        v.setTimeUntilNextStation(travelTime);

        bus.publish(new SimulationEvent(SimulationEvent.Kind.DEPARTURE, v, current, next, travelTime));
    }

    private boolean isBlocked(Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) {
                return c.isBlocked();
            }
        }
        return false;
    }

    private void tickInTransit(Vehicle v, List<Station> route) {
        double remaining = v.getTimeUntilNextStation() - 1.0;

        if (remaining > 0) {
            v.setTimeUntilNextStation(remaining);
            return;
        }

        int arrivedIndex = v.getRouteIndex() + 1;
        Station arrived = route.get(arrivedIndex);

        v.setRouteIndex(arrivedIndex);
        v.setState(VehicleState.DOCKED);
        v.setTimeUntilNextStation(0.0);
        v.setTimeParked(0.0);

        bus.publish(new SimulationEvent(SimulationEvent.Kind.ARRIVAL, v, null, arrived, 0.0));
    }

    private double getDistance(Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) {
                return c.getDistance();
            }
        }
        return 1.0;
    }
}