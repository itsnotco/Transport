/*
 * Modélise un tronçon bidirectionnel entre deux stations.
 * Stocke la distance (utilisée pour calculer les temps de trajet)
 * et un flag de blocage activé par un ConnectionIncident.
 */
package model;

public class Connection {

    private Station stationA;
    private Station stationB;
    private double distance;
    private boolean blocked = false;

    public Connection(Station stationA, Station stationB, double distance) {
        this.stationA = stationA;
        this.stationB = stationB;
        this.distance = distance;
    }

    // Vérifie si cette connexion implique une station donnée (fonctionne dans les deux sens A→B ou B→A).
    public boolean involves(Station s) {
        return stationA == s || stationB == s;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Station getStationA() {
        return stationA;
    }

    public Station getStationB() {
        return stationB;
    }

    public double getDistance() {
        return distance;
    }
}