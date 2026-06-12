/*
 * Gère le cycle de vie complet des passagers : spawn, embarquement, débarquement,
 * correspondances et recalcul d'itinéraire en temps réel.
 * À chaque tick, les passagers arrivés reçoivent un nouveau trajet aléatoire (reroll),
 * assurant un flux continu et permanent sur le réseau.
 */
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

    // Calcule l'itinéraire du passager et l'ajoute à la file d'attente de sa station d'origine.
    public void spawn(Passenger p, int now) {
        List<JourneyLeg> plan = router.findEarliestArrival(p.getOrigin(), p.getDestination(), vehicles, now);
        p.setPlan(plan);
        p.setCurrentStation(p.getOrigin());

        // Si aucun itinéraire n'existe ou si la station est saturée, marque le passager arrivé immédiatement.
        if (plan.isEmpty() || p.getOrigin().isFull()) {
            p.setState(PassengerState.ARRIVED);
        } else {
            p.setState(PassengerState.WAITING);
            p.getOrigin().addWaiting(p);
        }

        if (!passengers.contains(p)) {
            passengers.add(p);
        }
    }

    // Désigne aléatoirement un passager non arrivé comme passager suivi dans l'interface visuelle.
    public void pickNewTracked() {
        List<Passenger> candidates = new ArrayList<>();
        for (Passenger p : passengers) {
            if (p.getState() != PassengerState.ARRIVED) {
                candidates.add(p);
            }
        }
        if (candidates.isEmpty()) return;
        Passenger p = candidates.get(rnd.nextInt(candidates.size()));
        p.setTracked(true);
        System.out.println("  >> SUIVI : " + p.getId() + " (" + p.getOrigin().getName() + " -> " + p.getDestination().getName() + ")");
    }

    public void tick(int now) {
        // Relance un nouveau trajet pour chaque passager ayant atteint sa destination.
        for (Passenger p : new ArrayList<>(passengers)) {
            if (p.getState() == PassengerState.ARRIVED) {
                PassengerFactory.reroll(p, graph);
                spawn(p, now);
            }
        }

        // Traite les embarquements et débarquements sur chaque véhicule actuellement à quai.
        for (Vehicle v : vehicles) {
            if (!v.isParked()) continue;
            if (v.getState() == VehicleState.OUT_OF_SERVICE) continue;
            Station here = v.getCurrentStation();
            if (here == null || here.isClosed()) continue;
            alight(v, here, now);
            board(v, here, now);
        }

        // Recalcule le plan de chaque passager en attente (le réseau a pu changer suite à un incident).
        for (Passenger p : passengers) {
            if (p.getState() == PassengerState.WAITING) {
                recompute(p, now);
            }
        }
    }

    private void recompute(Passenger p, int now) {
        p.setPlan(router.findEarliestArrival(p.getCurrentStation(), p.getDestination(), vehicles, now));
    }

    // Débarque les passagers dont la station de descente prévue correspond à la station courante du véhicule.
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
            // Si d'autres tronçons restent dans le plan, le passager attend une correspondance.
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
                    // Dès qu'un passager suivi arrive à destination, un nouveau suivi est lancé immédiatement.
                    pickNewTracked();
                }
            }
        }
    }

    // Embarque les passagers en attente dont le plan désigne ce véhicule, dans la limite de capacité.
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