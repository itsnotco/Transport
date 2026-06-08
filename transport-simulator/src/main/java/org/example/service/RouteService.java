package org.example.service;

import org.example.model.Connection;
import org.example.model.Station;
import org.example.model.VehicleType;
import org.example.network.NetworkGraph;

import java.util.ArrayList;

public class RouteService {

    private NetworkGraph graph;

    public RouteService(NetworkGraph graph) {
        this.graph = graph;
    }

    public ArrayList<Station> findRoute(Station origin, Station destination, VehicleType type) {

        ArrayList<Station> stations = graph.getStations();
        int n = stations.size();

        double[] dist = new double[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            dist[i] = Double.MAX_VALUE;
            prev[i] = -1;
            visited[i] = false;
        }

        int originIndex = stations.indexOf(origin);
        dist[originIndex] = 0;

        for (int i = 0; i < n; i++) {

            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && (u == -1 || dist[j] < dist[u])) {
                    u = j;
                }
            }

            if (u == -1 || dist[u] == Double.MAX_VALUE) break;
            visited[u] = true;

            Station current = stations.get(u);
            ArrayList<Connection> connections = graph.getConnectionsFrom(current, type);

            for (Connection c : connections) {
                Station neighbor = c.getOther(current);
                int v = stations.indexOf(neighbor);

                if (dist[u] + c.getDistance() < dist[v]) {
                    dist[v] = dist[u] + c.getDistance();
                    prev[v] = u;
                }
            }
        }

        int destIndex = stations.indexOf(destination);
        if (prev[destIndex] == -1 && originIndex != destIndex) {
            return new ArrayList<Station>(); // pas de chemin
        }

        ArrayList<Station> path = new ArrayList<Station>();
        int current = destIndex;
        while (current != -1) {
            path.add(0, stations.get(current));
            current = prev[current];
        }

        return path;
    }
}