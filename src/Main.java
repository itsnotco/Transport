import display.AsciiDisplay;
import display.SwingDisplay;
import factory.GraphFactory;
import factory.PassengerFactory;
import factory.VehicleFactory;
import model.Passenger;
import model.Vehicle;
import network.NetworkGraph;
import observer.EventBus;
import service.RouteService;
import simulation.PassengerManager;
import simulation.TimeManager;
import simulation.VehicleScheduler;

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
        bus.subscribe(display);   // l'Observer logge les departs/arrivees dans le terminal

        PassengerManager passengers = new PassengerManager(router, vehicles, graph);

        int start = time.getCurrentMinute();

        // --- 4 passagers SUIVIS (affiches en detail dans le panneau Swing) ---
        List<Passenger> tracked = PassengerFactory.createFixed(graph);
        for (Passenger p : tracked) {
            p.setTracked(true);
            passengers.spawn(p, start);
        }

        // --- LA FOULE : 400 passagers aleatoires, recycles a l'arrivee ---
        List<Passenger> crowd = PassengerFactory.createRandom(graph, 400, 5);
        for (Passenger p : crowd) {
            passengers.spawn(p, start);
        }

        passengers.tick(start);   // montees a l'instant 0

        // --- fenetre graphique ---
        SwingDisplay swing = new SwingDisplay(graph, vehicles, passengers.getPassengers());
        swing.refresh(time.getFormattedTime(), passengers.getTotalArrivals());

        System.out.println("\n=== Entree = +1 min | q = quitter ===");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!scanner.hasNextLine()) {   // plus d'entree disponible -> on sort proprement
                break;
            }
            String input = scanner.nextLine();
            if (input.equals("q")) {
                break;
            }

            time.tick();
            int now = time.getCurrentMinute();

            scheduler.tick(vehicles);     // les vehicules avancent (evenements via le bus)
            passengers.tick(now);         // suivis recalcules + montees / descentes
            swing.refresh(time.getFormattedTime(), passengers.getTotalArrivals());

            if (now > 600) {              // garde-fou : ~10h simulees
                System.out.println("\n=== Arret (limite de temps) ===");
                break;
            }
        }

        System.out.println("=== FIN ===");
    }
}