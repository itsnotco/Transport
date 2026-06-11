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
import simulation.IncidentManager;
import simulation.PassengerManager;
import simulation.TimeManager;
import simulation.VehicleScheduler;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        NetworkGraph graph = GraphFactory.create();
        List<Vehicle> vehicles = VehicleFactory.create(graph);
        RouteService router = new RouteService(graph);
        AsciiDisplay display = new AsciiDisplay();
        TimeManager time = new TimeManager();

        EventBus bus = new EventBus();
        VehicleScheduler scheduler = new VehicleScheduler(graph, bus);
        bus.subscribe(display);

        PassengerManager passengers = new PassengerManager(router, vehicles, graph);
        IncidentManager incidents = new IncidentManager(graph, vehicles);

        int start = time.getCurrentMinute();

        List<Passenger> crowd = PassengerFactory.createRandom(graph, 400, 1);
        for (Passenger p : crowd) {
            passengers.spawn(p, start);
        }

        passengers.pickNewTracked();
        passengers.tick(start);

        SwingDisplay swing = new SwingDisplay(graph, vehicles, passengers.getPassengers(), incidents);
        swing.refresh(time.getFormattedTime(), passengers.getTotalArrivals(), start);

        System.out.println("\n=== Entree = +1 min | q = quitter ===");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine();
            if (input.equals("q")) {
                break;
            }

            time.tick();
            int now = time.getCurrentMinute();

            incidents.tick(now);
            scheduler.tick(vehicles);
            passengers.tick(now);
            swing.refresh(time.getFormattedTime(), passengers.getTotalArrivals(), now);
        }

        System.out.println("=== FIN ===");
    }
}