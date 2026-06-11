import factory.GraphFactory;
import factory.PassengerFactory;
import factory.VehicleFactory;
import display.SwingDisplay;
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

public class Main {

    public static void main(String[] args) {

        NetworkGraph graph = GraphFactory.create();
        List<Vehicle> vehicles = VehicleFactory.create(graph);
        RouteService router = new RouteService(graph);
        TimeManager time = new TimeManager();

        EventBus bus = new EventBus();
        VehicleScheduler scheduler = new VehicleScheduler(graph, bus);
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
        bus.subscribe(swing);
        swing.refresh(time.getFormattedTime(), passengers.getTotalArrivals(), start);

        swing.setOnStep(() -> {
            time.tick();
            int now = time.getCurrentMinute();

            incidents.tick(now);
            scheduler.tick(vehicles);
            passengers.tick(now);
            swing.refresh(time.getFormattedTime(), passengers.getTotalArrivals(), now);
        });


    }
}