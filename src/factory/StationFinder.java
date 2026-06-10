package factory;

import model.Station;
import network.NetworkGraph;

public class StationFinder {

    public static Station find(NetworkGraph graph, String id) {
        for (Station s : graph.getStations()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Station introuvable : " + id);
    }
}