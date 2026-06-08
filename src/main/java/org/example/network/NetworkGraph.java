package org.example.network;

import org.example.model.Connection;
import org.example.model.Station;
import org.example.model.VehicleType;

import java.util.ArrayList;

public class NetworkGraph {

    private ArrayList<Station> stations;
    private ArrayList<Connection> connections;

    public NetworkGraph() {
        this.stations = new ArrayList<Station>();
        this.connections = new ArrayList<Connection>();
    }

    public void addStation(Station s) {
        stations.add(s);
    }

    public void addConnection(Connection c) {
        connections.add(c);
    }

    public ArrayList<Connection> getConnectionsFrom(Station s, VehicleType type) {
        ArrayList<Connection> result = new ArrayList<Connection>();
        for (Connection c : connections) {
            if (c.involves(s) && c.supports(type)) {
                result.add(c);
            }
        }
        return result;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }
}