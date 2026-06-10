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
import service.JourneyLeg;
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
        bus.subscribe(display);

        // --- GRAPHIQUE (debut) ---
        SwingDisplay swing = new SwingDisplay(graph, vehicles);
        // --- GRAPHIQUE (fin) ---

        display.printNetwork(graph, vehicles);

        // --- PASSAGERS DE TEST : trajets fixes varies ---
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger("P-1",
                StationFinder.find(graph, "ST_AERO"),   // Aeroport
                StationFinder.find(graph, "ST_ACAC"),   // -> Les Acacias (traverse tout, multimodal)
                VehicleType.METRO));
        passengers.add(new Passenger("P-2",
                StationFinder.find(graph, "ST_CITE"),   // Cite Administrative
                StationFinder.find(graph, "ST_UNIV"),   // -> Universite (branche est, correspondances)
                VehicleType.METRO));
        passengers.add(new Passenger("P-3",
                StationFinder.find(graph, "ST_MOUL"),   // Moulin Vert
                StationFinder.find(graph, "ST_PORT"),   // -> Port Maritime (ouest, tram + metro)
                VehicleType.TRAM));
        passengers.add(new Passenger("P-4",
                StationFinder.find(graph, "ST_CENT"),   // Gare Centrale
                StationFinder.find(graph, "ST_PREF"),   // -> Prefecture (axe central, court)
                VehicleType.TRAIN));

        // Trajets calcules a l'instant initial
        printAll(router, display, passengers, vehicles, time.getCurrentMinute());

        System.out.println("\n=== SIMULATION — Entree = +1 min | q = quitter ===");
        Scanner scanner = new Scanner(System.in);

        // --- GRAPHIQUE (debut) ---
        swing.refresh();
        // --- GRAPHIQUE (fin) ---

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("q")) {
                break;
            }
            time.tick();
            int now = time.getCurrentMinute();

            display.printTime(time.getFormattedTime());
            scheduler.tick(vehicles);          // le reseau avance (evenements >> via le bus)

            // On RECALCULE le meilleur trajet de chaque passager depuis l'instant courant
            printAll(router, display, passengers, vehicles, now);

            // --- GRAPHIQUE (debut) ---
            swing.refresh();
            // --- GRAPHIQUE (fin) ---
        }

        System.out.println("=== FIN ===");
    }

    // Calcule et affiche le meilleur trajet de chaque passager a l'instant "now"
    private static void printAll(RouteService router, AsciiDisplay display,
                                 List<Passenger> passengers, List<Vehicle> vehicles, int now) {
        for (Passenger p : passengers) {
            List<JourneyLeg> legs =
                    router.findEarliestArrival(p.getOrigin(), p.getDestination(), vehicles, now);
            display.printJourney(p, legs, now);
        }
    }
}