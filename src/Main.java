import display.AsciiDisplay;
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

        PassengerManager passengers = new PassengerManager(router, vehicles, graph);

        // état partagé avec Python
        JsonExporter exporter = new JsonExporter(graph, "state.json");

        int start = time.getCurrentMinute();

        List<Passenger> tracked = PassengerFactory.createFixed(graph);
        for (Passenger p : tracked) {
            p.setTracked(true);
            passengers.spawn(p, start);
        }
        List<Passenger> crowd = PassengerFactory.createRandom(graph, 400, 5);
        for (Passenger p : crowd) {
            passengers.spawn(p, start);
        }

        passengers.tick(start);
        exporter.export(time.getFormattedTime(), vehicles, passengers.getPassengers());

        System.out.println("\n=== Entree = +1 min | q = quitter ===");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("q")) break;

            time.tick();
            int now = time.getCurrentMinute();

            scheduler.tick(vehicles);
            passengers.tick(now);

            exporter.export(time.getFormattedTime(), vehicles, passengers.getPassengers());

            if (now > 600) break;
        }
        System.out.println("=== FIN ===");
    }
}