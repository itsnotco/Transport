package service;

import model.Connection;
import model.Station;
import model.VehicleType;
import network.NetworkGraph;

import java.util.ArrayList;
import java.util.List;

public class RouteService {

    private NetworkGraph graph;

    public RouteService(NetworkGraph graph) {
        this.graph = graph;
    }

    public List<Station> findRoute(Station origin, Station destination, VehicleType type) {

        List<Station> stations = graph.getStations();
        int n = stations.size();

        double[] dist = new double[n];
        int[] prev    = new int[n];
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            dist[i]    = Double.MAX_VALUE;
            prev[i]    = -1;
            visited[i] = false;
        }

        int originIndex = stations.indexOf(origin);
        dist[originIndex] = 0;

        for (int step = 0; step < n; step++) {

            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && (u == -1 || dist[j] < dist[u])) {
                    u = j;
                }
            }

            if (u == -1 || dist[u] == Double.MAX_VALUE) {
                break;
            }

            visited[u] = true;

            Station current = stations.get(u);
            List<Connection> connections = graph.getConnectionsFrom(current, type);

            for (Connection c : connections) {
                Station neighbor = c.getOther(current);
                int v = stations.indexOf(neighbor);
                double newDist = dist[u] + c.getDistance();

                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    prev[v] = u;
                }
            }
        }

        int destIndex = stations.indexOf(destination);

        if (prev[destIndex] == -1 && originIndex != destIndex) {
            return new ArrayList<>();
        }

        List<Station> path = new ArrayList<>();
        int current = destIndex;

        while (current != -1) {
            path.add(0, stations.get(current));
            current = prev[current];
        }

        return path;
    }
}