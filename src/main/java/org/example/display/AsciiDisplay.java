package org.example.display;

import org.example.model.Station;
import org.example.model.Vehicle;
import org.example.network.NetworkGraph;
import org.example.observer.SimulationObserver;

import java.util.ArrayList;

public class AsciiDisplay implements SimulationObserver {

    // Observer : reçoit et affiche chaque événement de la simulation
    public void onEvent(String message) {
        System.out.println("  [EVENT] " + message);
    }

    // Affiche l'état global du réseau à chaque tick
    public void printStatus(NetworkGraph graph, ArrayList<Vehicle> vehicles, String time) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║  RÉSEAU - " + time + "                           ║");
        System.out.println("╠══════════════════════════════════════════╣");

        System.out.println("║  VÉHICULES :                             ║");
        for (Vehicle v : vehicles) {
            String location;
            if (v.getCurrentStation() != null) {
                location = "@ " + v.getCurrentStation().getName();
            } else {
                location = "en transit";
            }
            System.out.println("║    " + v.getType() + " [" + v.getId() + "] "
                    + v.getOnboard().size() + "/" + v.getCapacity()
                    + " pax - " + location + " [" + v.getState() + "]");
        }

        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  GARES EN ATTENTE :                      ║");
        for (Station s : graph.getStations()) {
            if (!s.getWaitingPassengers().isEmpty()) {
                System.out.println("║    " + s.getName()
                        + " : " + s.getWaitingPassengers().size() + " passager(s)");
            }
        }

        System.out.println("╚══════════════════════════════════════════╝");
    }
}