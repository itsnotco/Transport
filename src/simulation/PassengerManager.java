package simulation;

import factory.PassengerFactory;
import model.Passenger;
import model.PassengerState;
import model.Station;
import model.Vehicle;
import model.VehicleState;
import network.NetworkGraph;
import service.JourneyLeg;
import service.RouteService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PassengerManager {

    private final RouteService router;
    private final List<Vehicle> vehicles;
    private final NetworkGraph graph;
    private final List<Passenger> passengers = new ArrayList<>();
    private final Random rnd = new Random();

    private int totalArrivals = 0;

    public PassengerManager(RouteService router, List<Vehicle> vehicles, NetworkGraph graph) {
        this.router = router;
        this.vehicles = vehicles;
        this.graph = graph;
    }

    public void spawn(Passenger p, int now) {
        List<JourneyLeg> plan = router.findEarliestArrival(p.getOrigin(), p.getDestination(), vehicles, now);
        p.setPlan(plan);
        p.setCurrentStation(p.getOrigin());

        if (plan.isEmpty()) {
            p.setState(PassengerState.ARRIVED);
        } else {
            p.setState(PassengerState.WAITING);
            p.getOrigin().addWaiting(p);
        }

        if (!passengers.contains(p)) {
            passengers.add(p);
        }
    }

    public void pickNewTracked() {
        List<Passenger> candidates = new ArrayList<>();
        for (Passenger p : passengers) {
            if (p.getState() != PassengerState.ARRIVED) {
                candidates.add(p);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Passenger p = candidates.get(rnd.nextInt(candidates.size()));
        p.setTracked(true);
        System.out.println("  >> SUIVI : " + p.getId() + " (" + p.getOrigin().getName() + " -> " + p.getDestination().getName() + ")");
    }

    public void tick(int now) {
        for (Passenger p : new ArrayList<>(passengers)) {
            if (p.getState() == PassengerState.ARRIVED) {
                PassengerFactory.reroll(p, graph);
                spawn(p, now);
            }
        }

        for (Vehicle v : vehicles) {
            if (!v.isParked()) {
                continue;
            }
            if (v.getState() == VehicleState.OUT_OF_SERVICE) {
                continue;
            }
            Station here = v.getCurrentStation();
            if (here == null || here.isClosed()) {
                continue;
            }
            alight(v, here, now);
            board(v, here, now);
        }

        for (Passenger p : passengers) {
            if (p.getState() == PassengerState.WAITING) {
                recompute(p, now);
            }
        }
    }

    private void recompute(Passenger p, int now) {
        p.setPlan(router.findEarliestArrival(p.getCurrentStation(), p.getDestination(), vehicles, now));
    }

    private void alight(Vehicle v, Station here, int now) {
        List<Passenger> leaving = new ArrayList<>();
        for (Passenger p : v.getOnboard()) {
            JourneyLeg leg = p.getCurrentLeg();
            if (leg != null && leg.getTo() == here) {
                leaving.add(p);
            }
        }
        for (Passenger p : leaving) {
            v.removePassenger(p);
            p.setCurrentStation(here);
            p.advanceLeg();
            if (p.hasMoreLegs()) {
                p.setState(PassengerState.WAITING);
                here.addWaiting(p);
                recompute(p, now);
                if (p.isTracked()) {
                    System.out.println("  >> " + p.getId() + " descend a " + here.getName() + " (correspondance)");
                }
            } else {
                here.recordArrival();
                totalArrivals++;
                p.setState(PassengerState.ARRIVED);
                if (p.isTracked()) {
                    System.out.println("  >> " + p.getId() + " ARRIVE a " + here.getName() + " a " + hhmm(now));
                    p.setTracked(false);
                    pickNewTracked();
                }
            }
        }
    }

    private void board(Vehicle v, Station here, int now) {
        List<Passenger> boarding = new ArrayList<>();
        for (Passenger p : here.getWaiting()) {
            JourneyLeg leg = p.getCurrentLeg();
            if (leg != null
                    && leg.getFrom() == here
                    && leg.getVehicleId().equals(v.getId())
                    && v.getPassengerCount() + boarding.size() < v.getCapacity()) {
                boarding.add(p);
            }
        }
        for (Passenger p : boarding) {
            here.removeWaiting(p);
            v.addPassenger(p);
            p.setState(PassengerState.ON_BOARD);
            if (p.isTracked()) {
                System.out.println("  >> " + p.getId() + " monte dans " + v.getId() + " a " + here.getName() + " a " + hhmm(now));
            }
        }
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public int getTotalArrivals() {
        return totalArrivals;
    }

    private String hhmm(int minute) {
        int h = (minute / 60) % 24;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }
}