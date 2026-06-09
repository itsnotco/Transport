import display.AsciiDisplay;
import factory.GraphFactory;
import factory.StationFinder;
import factory.VehicleFactory;
import model.Passenger;
import model.Vehicle;
import model.VehicleType;
import network.NetworkGraph;
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
        VehicleScheduler scheduler = new VehicleScheduler(graph);

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

        display.printStatus(vehicles, time.getFormattedTime(), new ArrayList<>());

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("q")) {
                break;
            }
            time.tick();
            List<String> events = scheduler.tick(vehicles);
            display.printStatus(vehicles, time.getFormattedTime(), events);
        }

        System.out.println("=== FIN ===");
    }
}