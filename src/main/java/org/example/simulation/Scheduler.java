package org.example.simulation;

import org.example.model.Connection;
import org.example.model.Passenger;
import org.example.model.Station;
import org.example.model.Vehicle;
import org.example.network.NetworkGraph;
import org.example.observer.SimulationObservable;

import java.util.ArrayList;

public class Scheduler {

    private NetworkGraph graph;
    private SimulationObservable observable;

    private ArrayList<Vehicle> vehicles;
    private ArrayList<ArrayList<Station>> routes;
    private ArrayList<Integer> routeIndexes;
    private ArrayList<Double> timeRemaining;

    public Scheduler(NetworkGraph graph, SimulationObservable observable) {
        this.graph = graph;
        this.observable = observable;
        this.vehicles = new ArrayList<Vehicle>();
        this.routes = new ArrayList<ArrayList<Station>>();
        this.routeIndexes = new ArrayList<Integer>();
        this.timeRemaining = new ArrayList<Double>();
    }

    public void registerVehicle(Vehicle vehicle, ArrayList<Station> route) {
        vehicles.add(vehicle);
        routes.add(route);
        routeIndexes.add(0);
        timeRemaining.add(0.0);
        vehicle.setCurrentStation(route.get(0));
        observable.notifyObservers(vehicle.getType() + " [" + vehicle.getId() + "] enregistré à " + route.get(0).getName());
    }

    public void tick(int minutes) {
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle vehicle = vehicles.get(i);

            if (!vehicle.isOperational()) continue;

            ArrayList<Station> route = routes.get(i);
            int index = routeIndexes.get(i);
            double remaining = timeRemaining.get(i);

            if (!vehicle.isInTransit()) {
                Station current = route.get(index);
                deboardPassengers(vehicle, current);
                boardPassengers(vehicle, current, index, route);

                int nextIndex = (index + 1) % route.size();
                Station nextStation = route.get(nextIndex);
                double distance = getDistance(current, nextStation);
                double travelTime = distance / vehicle.getSpeed();

                routeIndexes.set(i, nextIndex);
                timeRemaining.set(i, travelTime);
                vehicle.setInTransit();

                observable.notifyObservers(vehicle.getType() + " [" + vehicle.getId() + "] quitte "
                        + current.getName() + " → " + nextStation.getName()
                        + " (" + String.format("%.1f", travelTime) + " min)");

            } else {
                remaining -= minutes;
                if (remaining <= 0) {
                    Station arrived = route.get(routeIndexes.get(i));
                    vehicle.setCurrentStation(arrived);
                    timeRemaining.set(i, 0.0);
                    observable.notifyObservers(vehicle.getType() + " [" + vehicle.getId() + "] arrivé à " + arrived.getName());
                } else {
                    timeRemaining.set(i, remaining);
                }
            }
        }
    }

    private void deboardPassengers(Vehicle vehicle, Station station) {
        ArrayList<Passenger> toAlight = new ArrayList<Passenger>();

        for (Passenger p : vehicle.getOnboard()) {
            if (station == p.getNextStop() || station == p.getDestination()) {
                toAlight.add(p);
            }
        }

        for (Passenger p : toAlight) {
            vehicle.alightPassenger(p);
            p.advanceRoute();
            if (p.hasArrived()) {
                observable.notifyObservers(p.getId() + " est arrivé à destination : " + station.getName());
            } else {
                station.addPassenger(p);
                observable.notifyObservers(p.getId() + " transfère à " + station.getName());
            }
        }
    }

    private void boardPassengers(Vehicle vehicle, Station station, int currentIndex, ArrayList<Station> route) {
        ArrayList<Passenger> toBoard = new ArrayList<Passenger>();

        for (Passenger p : station.getWaitingPassengers()) {
            if (vehicle.isFull()) break;
            Station nextStop = p.getNextStop();
            if (nextStop != null && isOnRoute(nextStop, currentIndex, route)) {
                toBoard.add(p);
            }
        }

        for (Passenger p : toBoard) {
            station.removePassenger(p);
            vehicle.boardPassenger(p);
            observable.notifyObservers(p.getId() + " monte dans " + vehicle.getType()
                    + " [" + vehicle.getId() + "] à " + station.getName());
        }
    }

    private boolean isOnRoute(Station target, int currentIndex, ArrayList<Station> route) {
        for (int i = currentIndex; i < route.size(); i++) {
            if (route.get(i) == target) return true;
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

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }
}