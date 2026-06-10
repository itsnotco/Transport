package factory;

import model.Passenger;
import model.Station;
import model.VehicleType;
import network.NetworkGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PassengerFactory {

    public static List<Passenger> createFixed(NetworkGraph graph) {
        List<Passenger> people = new ArrayList<>();
        people.add(new Passenger("P-1",
                StationFinder.find(graph, "ST_AERO"),
                StationFinder.find(graph, "ST_ACAC"),
                VehicleType.METRO));
        people.add(new Passenger("P-2",
                StationFinder.find(graph, "ST_CITE"),
                StationFinder.find(graph, "ST_UNIV"),
                VehicleType.METRO));
        people.add(new Passenger("P-3",
                StationFinder.find(graph, "ST_MOUL"),
                StationFinder.find(graph, "ST_PORT"),
                VehicleType.TRAM));
        people.add(new Passenger("P-4",
                StationFinder.find(graph, "ST_CENT"),
                StationFinder.find(graph, "ST_PREF"),
                VehicleType.TRAIN));
        return people;
    }

    public static List<Passenger> createRandom(NetworkGraph graph, int count, int startId) {
        List<Passenger> people = new ArrayList<>();
        List<Station> stations = graph.getStations();
        VehicleType[] types = VehicleType.values();
        Random rnd = new Random();

        for (int i = 0; i < count; i++) {
            Station origin = stations.get(rnd.nextInt(stations.size()));

            Station dest;
            do {
                dest = stations.get(rnd.nextInt(stations.size()));
            } while (dest == origin);

            VehicleType pref = types[rnd.nextInt(types.length)];

            people.add(new Passenger("P-" + (startId + i), origin, dest, pref));
        }
        return people;
    }
}