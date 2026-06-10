package display;

import model.Passenger;
import model.PassengerState;
import model.Station;
import model.Vehicle;
import network.NetworkGraph;
import observer.SimulationEvent;
import observer.SimulationObserver;
import service.JourneyLeg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsciiDisplay implements SimulationObserver {

    public void printNetwork(NetworkGraph graph, List<Vehicle> vehicles) {
        System.out.println("=== GARES ===");
        for (Station s : graph.getStations()) {
            System.out.println(s.getId() + " | " + s.getName() + " | " + s.getSupportedTypes());
        }

        System.out.println("\n=== CONNEXIONS ===");
        for (var c : graph.getConnections()) {
            System.out.println(c.getStationA().getName() + " <-> " + c.getStationB().getName() + " (" + c.getDistance() + " km)");
        }

        System.out.println("\n=== VEHICULES ===");
        for (Vehicle v : vehicles) {
            List<Station> route = v.getRoute();
            String start = route.isEmpty() ? "?" : route.get(0).getName();
            String end   = route.isEmpty() ? "?" : route.get(route.size() - 1).getName();
            System.out.println(v.getId() + " | " + v.getType()
                    + " | vitesse: " + v.getSpeed() + " km/min"
                    + " | route: " + start + " -> " + end);
        }
    }

    public void printTime(String time) {
        System.out.println("\n--- t=" + time + " ---");
    }

    // Observer : evenements du reseau (contexte qui explique pourquoi le trajet change)
    @Override
    public void onEvent(SimulationEvent e) {
        Vehicle v = e.getVehicle();
        String prefix = "  >> " + v.getId() + " [" + v.getType() + "] ";
        switch (e.getKind()) {
            case DEPARTURE:
                System.out.println(prefix + e.getFrom().getName() + " -> " + e.getTo().getName()
                        + " (" + String.format("%.1f", e.getTravelTime()) + " min)");
                break;
            case ARRIVAL:
                System.out.println(prefix + "arrive : " + e.getTo().getName());
                break;
        }
    }

    // --- AFFICHAGE PASSAGER : plan multimodal calcule a l'instant "now" ---
    public void printJourney(Passenger p, List<JourneyLeg> legs, int now) {
        System.out.println("\n=== TRAJET " + p.getId() + " | "
                + p.getOrigin().getName() + " -> " + p.getDestination().getName()
                + " (calcule a " + hhmm(now) + ") ===");

        if (legs.isEmpty()) {
            System.out.println("  Aucun trajet trouve (ou deja a destination).");
            return;
        }

        int prevTime = now;
        String prevVeh = null;
        for (JourneyLeg leg : legs) {
            if (prevVeh == null) {
                int wait = leg.getBoardTime() - now;
                if (wait > 0) System.out.println("  Attente " + wait + " min a " + leg.getFrom().getName());
            } else {
                int wait = leg.getBoardTime() - prevTime;
                System.out.println("  Correspondance a " + leg.getFrom().getName()
                        + (wait > 0 ? " (attente " + wait + " min)" : ""));
            }
            System.out.println("  [" + leg.getType() + " " + leg.getVehicleId() + "] "
                    + leg.getFrom().getName() + " " + hhmm(leg.getBoardTime())
                    + " -> " + leg.getTo().getName() + " " + hhmm(leg.getAlightTime()));
            prevTime = leg.getAlightTime();
            prevVeh = leg.getVehicleId();
        }
        int arrival = legs.get(legs.size() - 1).getAlightTime();
        System.out.println("  => Depart " + hhmm(now) + " | Arrivee " + hhmm(arrival)
                + " | Duree totale " + (arrival - now) + " min");
    }

    // --- Les 4 passagers SUIVIS : une ligne chacun, a chaque tick ---
    public void printTracked(List<Passenger> passengers) {
        System.out.println("--- SUIVIS ---");
        for (Passenger p : passengers) {
            if (!p.isTracked()) {
                continue;
            }
            String pos;
            if (p.getState() == PassengerState.WAITING) {
                pos = "attend a " + p.getCurrentStation().getName();
            } else if (p.getState() == PassengerState.ON_BOARD) {
                JourneyLeg leg = p.getCurrentLeg();
                pos = "a bord de " + (leg != null ? leg.getVehicleId() : "?")
                        + " -> " + (leg != null ? leg.getTo().getName() : "?");
            } else {
                pos = "arrive a " + p.getDestination().getName();
            }
            System.out.println("  " + p.getId() + " [" + p.getState() + "] " + pos);
        }
    }

    // --- La FOULE : compteurs agreges + gare la plus bondee + remplissage moyen ---
    public void printSummary(List<Passenger> passengers, List<Vehicle> vehicles) {
        int waiting = 0, onboard = 0, arrived = 0;
        Map<String, Integer> waitingPerStation = new HashMap<>();

        for (Passenger p : passengers) {
            if (p.getState() == PassengerState.WAITING) {
                waiting++;
                String name = p.getCurrentStation().getName();
                waitingPerStation.merge(name, 1, Integer::sum);
            } else if (p.getState() == PassengerState.ON_BOARD) {
                onboard++;
            } else {
                arrived++;
            }
        }

        System.out.println("--- RESUME (" + passengers.size() + " passagers) ---");
        System.out.println("  En attente : " + waiting + " | A bord : " + onboard + " | Arrives : " + arrived);

        // Remplissage moyen des vehicules
        int totalOnboard = 0, totalCap = 0;
        for (Vehicle v : vehicles) {
            totalOnboard += v.getPassengerCount();
            totalCap += v.getCapacity();
        }
        double fill = (totalCap == 0) ? 0 : 100.0 * totalOnboard / totalCap;
        System.out.println("  Remplissage moyen : " + String.format("%.1f", fill) + " %");

        // Gare la plus bondee : simple boucle, on garde le maximum.
        String topName = null;
        int topCount = 0;
        for (Map.Entry<String, Integer> e : waitingPerStation.entrySet()) {
            if (e.getValue() > topCount) {
                topCount = e.getValue();
                topName = e.getKey();
            }
        }
        if (topName != null) {
            System.out.println("  Gare la plus bondee : " + topName + " (" + topCount + " en attente)");
        }
    }

    private String hhmm(int minute) {
        int h = (minute / 60) % 24;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }
}