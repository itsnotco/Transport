package service;

import model.Connection;
import model.Station;
import model.Vehicle;
import model.VehicleType;
import network.NetworkGraph;

import java.util.ArrayList;
import java.util.List;

public class RouteService {

    private static final int PARK = 1;
    private static final int TRANSFER = 1;
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

        List<TimedHop> hops = new ArrayList<>();
        for (Vehicle v : vehicles) {
            project(v, now, hops);
        }

        List<Station> stations = graph.getStations();
        int n = stations.size();

        int[] arr          = new int[n];
        int[] prev         = new int[n];
        TimedHop[] prevHop = new TimedHop[n];
        boolean[] visited  = new boolean[n];

        for (int i = 0; i < n; i++) {
            arr[i]  = Integer.MAX_VALUE;
            prev[i] = -1;
        }
        arr[stations.indexOf(origin)] = now;

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

            int minDep = (current == origin) ? arr[u] : arr[u] + TRANSFER;

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

        List<TimedHop> path = new ArrayList<>();
        int cur = destIndex;
        while (prev[cur] != -1) {
            path.add(0, prevHop[cur]);
            cur = prev[cur];
        }

        return buildLegs(path);
    }

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
            if (h.vehicleId.equals(vehId)) {
                legTo = h.to;
                alight = h.arr;
            } else {
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

    private void project(Vehicle v, int now, List<TimedHop> out) {
        List<Station> route = v.getRoute();
        int n = route.size();
        if (n < 2) return;

        int idx;
        int dep;

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
            if (idx >= n - 1) {
                from = 0; to = 1;
            }   // terminus : on reboucle au depart
            else to = idx + 1;

            Station a = route.get(from);
            Station b = route.get(to);
            int travel = Math.max(1, (int) Math.ceil(distance(a, b) / v.getSpeed()));
            int arrTime = dep + travel;

            out.add(new TimedHop(a, b, dep, arrTime, v.getId(), v.getType()));

            idx = to;
            dep = arrTime + PARK;
            hops++;
        }
    }

    private double distance(Station a, Station b) {
        for (Connection c : graph.getConnections())
            if (c.involves(a) && c.involves(b)) return c.getDistance();
        return 1.0;
    }

    private static class TimedHop {
        final Station from, to;
        final int dep, arr;
        final String vehicleId;
        final VehicleType type;

        TimedHop(Station from, Station to, int dep, int arr, String vehicleId, VehicleType type) {
            this.from = from; this.to = to;
            this.dep = dep; this.arr = arr;
            this.vehicleId = vehicleId; this.type = type;
        }
    }
}