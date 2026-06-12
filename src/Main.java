/*
 * Point d'entrée de l'application.
 * Initialise tous les composants de la simulation (graphe, véhicules, passagers,
 * incidents, affichage) et les relie entre eux avant de déléguer le contrôle
 * à l'interface Swing via le callback onStep.
 */

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

        // Construit le graphe du réseau (stations + connexions) et les véhicules qui y circulent.
        NetworkGraph graph = GraphFactory.create();
        List<Vehicle> vehicles = VehicleFactory.create(graph);
        RouteService router = new RouteService(graph);
        TimeManager time = new TimeManager();

        // Crée le bus d'événements et les gestionnaires de simulation.
        EventBus bus = new EventBus();
        VehicleScheduler scheduler = new VehicleScheduler(graph, bus);
        PassengerManager passengers = new PassengerManager(router, vehicles, graph);
        IncidentManager incidents = new IncidentManager(graph, vehicles);

        int start = time.getCurrentMinute();

        // Génère 400 passagers avec des origines/destinations aléatoires et calcule leur itinéraire initial.
        List<Passenger> crowd = PassengerFactory.createRandom(graph, 400, 1);
        for (Passenger p : crowd) {
            passengers.spawn(p, start);
        }

        // Choisit aléatoirement un passager à suivre visuellement dès le départ.
        passengers.pickNewTracked();
        passengers.tick(start);

        // Instancie l'affichage Swing et l'abonne au bus pour recevoir les événements de mouvement.
        SwingDisplay swing = new SwingDisplay(graph, vehicles, passengers.getPassengers(), incidents);
        bus.subscribe(swing);
        swing.refresh(time.getFormattedTime(), passengers.getTotalArrivals(), start);

        // Chaque appui sur ENTRÉE avance la simulation d'une minute : incidents, véhicules, puis passagers.
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