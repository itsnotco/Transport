import display.AsciiDisplay;
import display.SwingDisplay;          // <-- GRAPHIQUE
import factory.GraphFactory;
import factory.StationFinder;
import factory.VehicleFactory;
import model.Passenger;
import model.Vehicle;
import model.VehicleType;
import network.NetworkGraph;
import observer.EventBus;
import service.RouteService;
import simulation.TimeManager;
import simulation.VehicleScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        NetworkGraph graph         = GraphFactory.create();
        List<Vehicle> vehicles     = VehicleFactory.create(graph);
        RouteService router        = new RouteService(graph);
        AsciiDisplay display       = new AsciiDisplay();
        TimeManager time           = new TimeManager();

        EventBus bus               = new EventBus();
        VehicleScheduler scheduler = new VehicleScheduler(graph, bus);

        // Observer : l'affichage console s'abonne au bus
        bus.subscribe(display);

        // --- GRAPHIQUE (debut) : fenetre Swing, a supprimer pour revenir au tout-console ---
        SwingDisplay swing = new SwingDisplay(graph, vehicles);
        // --- GRAPHIQUE (fin) ---

        display.printNetwork(graph, vehicles);

        List<Passenger> passengers = new ArrayList<>();

        Passenger p1 = new Passenger("P-1",
                StationFinder.find(graph, "ST_AERO"),
                StationFinder.find(graph, "ST_CITE"),
                VehicleType.METRO);
        Passenger p2 = new Passenger("P-2",
                StationFinder.find(graph, "ST_CENT"),
                StationFinder.find(graph, "ST_ACAC"),
                VehicleType.TRAM);
        Passenger p3 = new Passenger("P-3",
                StationFinder.find(graph, "ST_CENT"),
                StationFinder.find(graph, "ST_PARC"),
                VehicleType.TRAIN);

        passengers.add(p1);
        passengers.add(p2);
        passengers.add(p3);

        for (Passenger p : passengers) {
            p.setRoute(router.findRoute(p.getOrigin(), p.getDestination(), p.getPreferredType()));
            display.printPassengerRoute(p);
        }

        System.out.println("\n=== SIMULATION — Entree = +1 min | q = quitter ===");
        Scanner scanner = new Scanner(System.in);

        display.printTime(time.getFormattedTime());
        display.printVehicles(vehicles);

        // --- GRAPHIQUE (debut) : dessin initial ---
        swing.refresh();
        // --- GRAPHIQUE (fin) ---

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("q")) {
                break;
            }
            time.tick();
            display.printTime(time.getFormattedTime());   // 1) en-tete
            scheduler.tick(vehicles);                      // 2) evenements pousses -> display.onEvent
            display.printVehicles(vehicles);               // 3) positions

            // --- GRAPHIQUE (debut) : rafraichissement apres chaque tick ---
            swing.refresh();
            // --- GRAPHIQUE (fin) ---
        }

        System.out.println("=== FIN ===");
    }
}