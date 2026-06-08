package org.example.service;

import org.example.model.Passenger;
import org.example.model.Station;
import org.example.model.VehicleType;
import org.example.observer.SimulationObservable;

import java.util.ArrayList;

public class PassengerService {

    private RouteService routeService;
    private ArrayList<Passenger> allPassengers;
    private int counter;

    public PassengerService(RouteService routeService) {
        this.routeService = routeService;
        this.allPassengers = new ArrayList<Passenger>();
        this.counter = 0;
    }

    public Passenger spawnPassenger(Station origin, Station destination, VehicleType type, SimulationObservable observable) {
        counter++;
        String id = "PASS_" + counter;

        Passenger p = new Passenger(id, origin, destination);
        ArrayList<Station> route = routeService.findRoute(origin, destination, type);

        if (route.isEmpty()) {
            observable.notifyObservers("Aucun trajet trouvé pour " + id + " (" + origin.getName() + " → " + destination.getName() + ")");
            return null;
        }

        p.setRoute(route);
        origin.addPassenger(p);
        allPassengers.add(p);

        observable.notifyObservers(id + " apparu à " + origin.getName() + " → " + destination.getName());
        return p;
    }

    public ArrayList<Passenger> getAllPassengers() {
        return allPassengers;
    }
}