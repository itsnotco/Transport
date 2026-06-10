package simulation;

import model.Passenger;
import model.PassengerState;
import model.Station;
import model.Vehicle;
import service.JourneyLeg;
import service.RouteService;

import java.util.ArrayList;
import java.util.List;

public class PassengerManager {

    private final RouteService router;
    private final List<Vehicle> vehicles;
    private final List<Passenger> passengers = new ArrayList<>();

    public PassengerManager(RouteService router, List<Vehicle> vehicles) {
        this.router = router;
        this.vehicles = vehicles;
    }

    // Apparition d'un passager a sa gare d'origine, plan calcule a "now".
    public void spawn(Passenger p, int now) {
        List<JourneyLeg> plan = router.findEarliestArrival(p.getOrigin(), p.getDestination(), vehicles, now);
        p.setPlan(plan);
        p.setCurrentStation(p.getOrigin());

        if (plan.isEmpty()) {
            p.setState(PassengerState.ARRIVED);   // deja a destination ou aucun trajet
            p.setLastShownArrival(-1);
        } else {
            p.setState(PassengerState.WAITING);
            p.getOrigin().addWaiting(p);
            p.setLastShownArrival(plan.get(plan.size() - 1).getAlightTime());
        }
        passengers.add(p);
    }

    // A appeler chaque minute, APRES scheduler.tick(...)
    public void tick(int now) {
        // 1) Les suivis qui attendent recalculent leur trajet (reagit a la congestion)
        for (Passenger p : passengers) {
            if (p.isTracked() && p.getState() == PassengerState.WAITING) {
                reassess(p, now);
            }
        }

        // 2) Montees / descentes a chaque vehicule a quai
        for (Vehicle v : vehicles) {
            if (!v.isParked()) {
                continue;
            }
            Station here = v.getCurrentStation();
            if (here == null) {
                continue;
            }
            alight(v, here, now);   // d'abord descendre
            board(v, here, now);    // puis monter
        }
    }

    // Recalcule le plan depuis la gare actuelle ; annonce si l'horaire d'arrivee change.
    private void reassess(Passenger p, int now) {
        List<JourneyLeg> plan =
                router.findEarliestArrival(p.getCurrentStation(), p.getDestination(), vehicles, now);
        p.setPlan(plan);

        int newArr = plan.isEmpty() ? -1 : plan.get(plan.size() - 1).getAlightTime();
        if (newArr != p.getLastShownArrival()) {
            if (plan.isEmpty()) {
                System.out.println("  >> " + p.getId() + " : aucun trajet depuis " + p.getCurrentStation().getName());
            } else {
                System.out.println("  >> " + p.getId() + " : arrivee revisee a " + hhmm(newArr)
                        + " (depuis " + p.getCurrentStation().getName() + ")");
            }
            p.setLastShownArrival(newArr);
        }
    }

    // Descendre : passager a bord dont l'etape en cours se TERMINE ici.
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
                if (p.isTracked()) {
                    System.out.println("  >> " + p.getId() + " descend a " + here.getName() + " (correspondance)");
                }
            } else {
                p.setState(PassengerState.ARRIVED);
                if (p.isTracked()) {
                    System.out.println("  >> " + p.getId() + " ARRIVE a " + here.getName() + " a " + hhmm(now));
                }
            }
        }
    }

    // Monter : passager qui attend ici ET dont l'etape demande CE vehicule, si place dispo.
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

    private String hhmm(int minute) {
        int h = (minute / 60) % 24;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }
}