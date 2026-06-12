/*
 * Fabrique de passagers pour la simulation.
 * createRandom génère une population initiale avec des trajets aléatoires.
 * reroll réassigne une nouvelle origine/destination à un passager arrivé,
 * en excluant systématiquement les stations actuellement fermées.
 */
package factory;

import model.Passenger;
import model.Station;
import network.NetworkGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PassengerFactory {

    public static List<Passenger> createRandom(NetworkGraph graph, int count, int startId) {
        List<Passenger> people = new ArrayList<>();
        List<Station> stations = graph.getStations();
        Random rnd = new Random();

        for (int i = 0; i < count; i++) {
            Station origin = stations.get(rnd.nextInt(stations.size()));

            // La destination doit obligatoirement différer de l'origine pour garantir un trajet réel.
            Station dest;
            do {
                dest = stations.get(rnd.nextInt(stations.size()));
            } while (dest == origin);

            people.add(new Passenger("P-" + (startId + i), origin, dest));
        }
        return people;
    }

    // Réattribue une origine et une destination aléatoires parmi les stations ouvertes uniquement.
    public static void reroll(Passenger p, NetworkGraph graph) {
        List<Station> open = new ArrayList<>();
        for (Station s : graph.getStations()) {
            if (!s.isClosed()) open.add(s);
        }
        if (open.size() < 2) return;

        Random rnd = new Random();
        Station origin = open.get(rnd.nextInt(open.size()));
        Station dest;
        do {
            dest = open.get(rnd.nextInt(open.size()));
        } while (dest == origin);

        p.setOrigin(origin);
        p.setDestination(dest);
    }
}