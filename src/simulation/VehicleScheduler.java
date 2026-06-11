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
        Station here = route.get(v.getRouteIndex());

        if (!here.isClosed()) {
            double parked = v.getTimeParked() + 1.0;
            if (parked < PARK_DURATION) {
                v.setTimeParked(parked);
                return;
            }
        }

        int nextIndex = v.getRouteIndex() + 1;
        if (nextIndex >= route.size()) {
            v.setRouteIndex(0);
            nextIndex = 1;
        }

        Station current = route.get(v.getRouteIndex());
        Station next = route.get(nextIndex);
        
        if (isBlocked(current, next)) {
            v.setTimeParked(0.0);
            return;
        }

        double travelTime = getDistance(current, next) / v.getSpeed();

        v.setState(VehicleState.IN_TRANSIT);
        v.setTimeParked(0.0);
        v.setTimeUntilNextStation(travelTime);

        bus.publish(new SimulationEvent(SimulationEvent.Kind.DEPARTURE, v, current, next));
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

        bus.publish(new SimulationEvent(SimulationEvent.Kind.ARRIVAL, v, null, arrived));
    }

    private boolean isBlocked(Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) {
                return c.isBlocked();
            }
        }
        return false;
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