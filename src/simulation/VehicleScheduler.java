/*
 * Orchestre le déplacement de tous les véhicules à chaque pas de simulation.
 * Distingue deux comportements : véhicule à quai (tickParked) et en transit (tickInTransit).
 * Publie des SimulationEvent sur le bus à chaque départ et arrivée.
 */
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

    // Durée minimale d'arrêt en station avant autorisation de départ (en minutes de simulation).
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

        // Accumule le temps d'arrêt ; si la durée minimale n'est pas atteinte, le véhicule attend encore.
        if (!here.isClosed()) {
            double parked = v.getTimeParked() + 1.0;
            if (parked < PARK_DURATION) {
                v.setTimeParked(parked);
                return;
            }
        }

        // Passe à l'index suivant en bouclant sur la route si le dernier arrêt est atteint.
        int nextIndex = v.getRouteIndex() + 1;
        if (nextIndex >= route.size()) {
            v.setRouteIndex(0);
            nextIndex = 1;
        }

        Station current = route.get(v.getRouteIndex());
        Station next = route.get(nextIndex);

        // Ne démarre pas si le tronçon suivant est actuellement bloqué par un incident.
        if (isBlocked(current, next)) {
            v.setTimeParked(0.0);
            return;
        }

        double travelTime = getDistance(current, next) / v.getSpeed();

        // Transition vers IN_TRANSIT : fixe le temps de parcours et notifie les observateurs.
        v.setState(VehicleState.IN_TRANSIT);
        v.setTimeParked(0.0);
        v.setTimeUntilNextStation(travelTime);

        bus.publish(new SimulationEvent(SimulationEvent.Kind.DEPARTURE, v, current, next));
    }

    private void tickInTransit(Vehicle v, List<Station> route) {
        double remaining = v.getTimeUntilNextStation() - 1.0;

        // Décrémente le compteur de transit ; si non nul, le trajet est encore en cours.
        if (remaining > 0) {
            v.setTimeUntilNextStation(remaining);
            return;
        }

        // Détermine l'index de la station d'arrivée avec bouclage en fin de route.
        int arrivedIndex = v.getRouteIndex() + 1;
        if (arrivedIndex >= route.size()) {
            arrivedIndex = 0;
        }

        Station arrived = route.get(arrivedIndex);
        v.setRouteIndex(arrivedIndex);

        // Si la station d'arrivée est fermée, le véhicule continue directement vers la suivante sans s'arrêter.
        if (arrived.isClosed()) {
            int nextIndex = arrivedIndex + 1;
            if (nextIndex >= route.size()) {
                nextIndex = 0;
            }
            Station next = route.get(nextIndex);

            if (!isBlocked(arrived, next)) {
                double travelTime = getDistance(arrived, next) / v.getSpeed();
                v.setTimeUntilNextStation(travelTime);
                bus.publish(new SimulationEvent(SimulationEvent.Kind.DEPARTURE, v, arrived, next));
            } else {
                // Tronçon suivant également bloqué : le véhicule immobilisé attend à la station fermée.
                v.setState(VehicleState.DOCKED);
                v.setTimeUntilNextStation(0.0);
                v.setTimeParked(0.0);
            }
            return;
        }

        // Arrivée normale : passe en DOCKED et publie l'événement d'arrivée vers les observateurs.
        v.setState(VehicleState.DOCKED);
        v.setTimeUntilNextStation(0.0);
        v.setTimeParked(0.0);

        bus.publish(new SimulationEvent(SimulationEvent.Kind.ARRIVAL, v, null, arrived));
    }

    private boolean isBlocked(Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) return c.isBlocked();
        }
        return false;
    }

    private double getDistance(Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) return c.getDistance();
        }
        return 1.0;
    }
}