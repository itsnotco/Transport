package network;

import model.Connection;
import model.Station;
import model.VehicleType;

import java.util.ArrayList;
import java.util.List;

public class NetworkGraph {

    private List<Station> stations;
    private List<Connection> connections;

    public NetworkGraph() {
        this.stations = new ArrayList<>();
        this.connections = new ArrayList<>();
    }

    public void addStation(Station s) {
        stations.add(s);
    }

    public void addConnection(Connection c) {
        connections.add(c);
    }

    public List<Connection> getConnectionsFrom(Station s, VehicleType type) {
        List<Connection> result = new ArrayList<>();
        for (Connection c : connections) {
            if (c.involves(s) && c.supports(type)) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Station> getStations() {
        return stations;
    }

    public List<Connection> getConnections() {
        return connections;
    }
}