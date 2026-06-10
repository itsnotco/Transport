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
        bus.subscribe(display);

        PassengerManager passengers = new PassengerManager(router, vehicles);

        SwingDisplay swing = new SwingDisplay(graph, vehicles);

        display.printNetwork(graph, vehicles);

        int start = time.getCurrentMinute();

        List<Passenger> tracked = PassengerFactory.createFixed(graph);
        for (Passenger p : tracked) {
            p.setTracked(true);
            passengers.spawn(p, start);
            display.printJourney(p, p.getPlan(), start);   // plan initial complet
        }

        List<Passenger> crowd = PassengerFactory.createRandom(graph, 400, 5);   // P-5..P-404
        for (Passenger p : crowd) {
            passengers.spawn(p, start);
        }

        passengers.tick(start);
        display.printTracked(passengers.getPassengers());
        display.printSummary(passengers.getPassengers(), vehicles);

        System.out.println("\n=== SIMULATION — Entree = +1 min | q = quitter ===");
        Scanner scanner = new Scanner(System.in);
        swing.refresh();

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("q")) {
                break;
            }

            time.tick();
            int now = time.getCurrentMinute();

            display.printTime(time.getFormattedTime());
            scheduler.tick(vehicles);
            passengers.tick(now);

            display.printTracked(passengers.getPassengers());
            display.printSummary(passengers.getPassengers(), vehicles);
            swing.refresh();

            if (now > 600) {
                System.out.println("\n=== Arret (limite de temps) ===");
                break;
            }
        }

        System.out.println("=== FIN ===");
    }
}