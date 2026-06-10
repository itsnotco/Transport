package display;

import model.Passenger;
import model.Station;
import model.Vehicle;
import network.NetworkGraph;
import observer.SimulationEvent;
import observer.SimulationObserver;
import service.JourneyLeg;

import java.util.List;

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

    // --- AFFICHAGE PASSAGER : meilleur trajet multimodal calcule a l'instant "now" ---
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

    // Gardee mais non utilisee dans le mode test (on ne montre plus l'etat des transports)
    public void printVehicles(List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            String position;
            if (v.isParked()) {
                position = "EN GARE : " + v.getCurrentStation().getName();
            } else {
                Station next = v.getNextStation();
                long minutes = (long) Math.ceil(Math.max(0, v.getTimeUntilNextStation()));
                position = "EN TRANSIT -> "
                        + (next != null ? next.getName() : "?")
                        + " (encore " + minutes + " min)";
            }
            System.out.println("  " + v.getId() + " [" + v.getType() + "] " + position);
        }
    }

    private String hhmm(int minute) {
        int h = (minute / 60) % 24;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }
}