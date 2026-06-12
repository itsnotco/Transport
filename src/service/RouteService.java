/*
 * Calcule l'itinéraire optimal (arrivée au plus tôt) pour un passager.
 * Implémente une variante du CSA (Connection Scan Algorithm) :
 * les trajets futurs de chaque véhicule sont projetés dans une fenêtre HORIZON,
 * puis un Dijkstra temporel détermine le chemin le plus rapide station par station.
 */
package service;

import model.Connection;
import model.Station;
import model.Vehicle;
import model.VehicleState;
import model.VehicleType;
import network.NetworkGraph;

import java.util.ArrayList;
import java.util.List;

public class RouteService {

    // Temps d'arrêt à quai (1 min) et pénalité de correspondance entre deux véhicules différents.
    private static final int PARK = 1;
    private static final int TRANSFER = 1;
    // Fenêtre de projection maximale (en minutes) et limite de sauts simulés par véhicule.
    private static final int HORIZON = 300;
    private static final int MAX_HOPS_PER_VEHICLE = 500;

    private final NetworkGraph graph;

    public RouteService(NetworkGraph graph) {
        this.graph = graph;
    }

    public List<JourneyLeg> findEarliestArrival(Station origin, Station destination, List<Vehicle> vehicles, int now) {

        if (origin == destination) {
            return new ArrayList<>();
        }

        // Projette les positions futures de tous les véhicules en une liste de sauts horodatés.
        List<TimedHop> hops = new ArrayList<>();
        for (Vehicle v : vehicles) {
            project(v, now, hops);
        }

        List<Station> stations = graph.getStations();
        int n = stations.size();

        // arr[i] = minute d'arrivée la plus tôt connue à la station i ; Integer.MAX_VALUE = non atteint.
        int[] arr = new int[n];
        int[] prev = new int[n];
        TimedHop[] prevHop = new TimedHop[n];
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            arr[i] = Integer.MAX_VALUE;
            prev[i] = -1;
        }
        arr[stations.indexOf(origin)] = now;

        // Dijkstra temporel : traite à chaque itération la station atteignable le plus tôt.
        for (int step = 0; step < n; step++) {

            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && (u == -1 || arr[j] < arr[u])) {
                    u = j;
                }
            }
            if (u == -1 || arr[u] == Integer.MAX_VALUE) {
                break;
            }
            visited[u] = true;

            Station current = stations.get(u);

            // À l'origine, pas de pénalité de correspondance ; ailleurs, on ajoute TRANSFER.
            int minDep = (current == origin) ? arr[u] : arr[u] + TRANSFER;

            // Pour chaque saut disponible depuis cette station, met à jour arr[] si gain de temps.
            for (TimedHop h : hops) {
                if (h.from == current && h.dep >= minDep) {
                    int v = stations.indexOf(h.to);
                    if (h.arr < arr[v]) {
                        arr[v] = h.arr;
                        prev[v] = u;
                        prevHop[v] = h;
                    }
                }
            }
        }

        int destIndex = stations.indexOf(destination);
        if (arr[destIndex] == Integer.MAX_VALUE) {
            return new ArrayList<>();
        }

        // Reconstitue le chemin de sauts en remontant le tableau prev depuis la destination.
        List<TimedHop> path = new ArrayList<>();
        int cur = destIndex;
        while (prev[cur] != -1) {
            path.add(0, prevHop[cur]);
            cur = prev[cur];
        }

        return buildLegs(path);
    }

    // Regroupe les sauts consécutifs du même véhicule en un seul JourneyLeg pour simplifier le plan passager.
    private List<JourneyLeg> buildLegs(List<TimedHop> path) {
        List<JourneyLeg> legs = new ArrayList<>();
        if (path.isEmpty()) {
            return legs;
        }

        TimedHop first = path.get(0);
        String vehId = first.vehicleId;
        VehicleType type = first.type;
        Station legFrom = first.from;
        int board = first.dep;
        Station legTo = first.to;
        int alight = first.arr;

        for (int i = 1; i < path.size(); i++) {
            TimedHop h = path.get(i);
            // Même véhicule : étend le tronçon courant jusqu'à la station suivante.
            if (h.vehicleId.equals(vehId)) {
                legTo = h.to;
                alight = h.arr;
            } else {
                // Changement de véhicule : clôture le tronçon précédent et en démarre un nouveau.
                legs.add(new JourneyLeg(vehId, type, legFrom, legTo, board, alight));
                vehId = h.vehicleId;
                type = h.type;
                legFrom = h.from;
                board = h.dep;
                legTo = h.to;
                alight = h.arr;
            }
        }
        legs.add(new JourneyLeg(vehId, type, legFrom, legTo, board, alight));
        return legs;
    }

    // Projette les sauts futurs d'un véhicule sur sa route cyclique dans la fenêtre HORIZON.
    private void project(Vehicle v, int now, List<TimedHop> out) {
        List<Station> route = v.getRoute();
        int n = route.size();
        if (n < 2) return;

        if (v.getState() == VehicleState.OUT_OF_SERVICE) return;

        int idx;
        int dep;

        // Calcule le prochain départ selon que le véhicule est à l'arrêt ou encore en transit.
        if (v.isParked()) {
            idx = v.getRouteIndex();
            dep = now + PARK;
        } else {
            idx = v.getRouteIndex() + 1;
            int arriveNext = now + (int) Math.ceil(v.getTimeUntilNextStation());
            dep = arriveNext + PARK;
        }

        int hops = 0;
        while (dep <= now + HORIZON && hops < MAX_HOPS_PER_VEHICLE) {
            int from = idx;
            int to;
            // Reboucle sur le début de la route quand le dernier arrêt est atteint.
            if (idx >= n - 1) {
                from = 0;
                to = 1;
            } else to = idx + 1;

            Station a = route.get(from);
            Station b = route.get(to);
            int travel = Math.max(1, (int) Math.ceil(distance(a, b) / v.getSpeed()));

            // N'enregistre le saut que si les deux stations et le tronçon sont opérationnels.
            if (!a.isClosed() && !b.isClosed() && !isBlocked(a, b)) {
                int arrTime = dep + travel;
                out.add(new TimedHop(a, b, dep, arrTime, v.getId(), v.getType()));
                dep = arrTime + PARK;
            } else {
                dep = dep + travel + PARK;
            }

            idx = to;
            hops++;
        }
    }

    private boolean isBlocked(Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) {
                return c.isBlocked();
            }
        }
        return false;
    }

    private double distance(Station a, Station b) {
        for (Connection c : graph.getConnections())
            if (c.involves(a) && c.involves(b)) return c.getDistance();
        return 1.0;
    }

    // Représentation interne d'un trajet élémentaire : un véhicule entre deux stations à des instants précis.
    private static class TimedHop {
        final Station from, to;
        final int dep, arr;
        final String vehicleId;
        final VehicleType type;

        TimedHop(Station from, Station to, int dep, int arr, String vehicleId, VehicleType type) {
            this.from = from;
            this.to = to;
            this.dep = dep;
            this.arr = arr;
            this.vehicleId = vehicleId;
            this.type = type;
        }
    }
}