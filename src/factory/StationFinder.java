/*
 * Utilitaire de résolution de station par identifiant.
 * Centralisé ici pour éviter la duplication de la boucle de recherche
 * dans GraphFactory et VehicleFactory.
 */
package factory;

import model.Station;
import network.NetworkGraph;

public class StationFinder {

    // Lance une exception explicite si l'identifiant est introuvable, évitant les NullPointerException silencieuses.
    public static Station find(NetworkGraph graph, String id) {
        for (Station s : graph.getStations()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Station introuvable : " + id);
    }
}