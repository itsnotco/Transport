package display;

import model.Passenger;
import model.Station;
import model.Vehicle;
import network.NetworkGraph;

import java.util.List;

public class AsciiDisplay {

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

    public void printStatus(List<Vehicle> vehicles, String time, List<String> events) {
        System.out.println("\n--- t=" + time + " ---");

        for (String event : events) {
            System.out.println("  >> " + event);
        }

        for (Vehicle v : vehicles) {
            String position;
            if (v.isParked()) {
                position = "EN GARE : " + v.getCurrentStation().getName();
            } else {
                Station next = v.getNextStation();
                position = "EN TRANSIT -> "
                        + (next != null ? next.getName() : "?")
                        + " (encore " + String.format("%.0f", v.getTimeUntilNextStation()) + " min)";
            }
            System.out.println("  " + v.getId() + " [" + v.getType() + "] " + position);
        }
    }

    public void printPassengerRoute(Passenger p) {
        System.out.println("\n=== TRAJET " + p.getId() + " ===");
        System.out.println("De : " + p.getOrigin().getName() + " -> " + p.getDestination().getName());
        System.out.println("Transport : " + p.getPreferredType());

        List<Station> route = p.getRoute();
        if (route == null || route.isEmpty()) {
            System.out.println("Aucun trajet trouve.");
            return;
        }

        for (int i = 0; i < route.size(); i++) {
            if (i > 0) System.out.print(" -> ");
            System.out.print(route.get(i).getName());
        }
        System.out.println();
    }
}