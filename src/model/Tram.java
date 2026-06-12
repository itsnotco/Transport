/*
 * Tram : le véhicule de surface le plus lent (0.5), adapté aux courtes distances urbaines.
 * Couvre les boucles de quartier avec une granularité d'arrêts plus fine.
 */
package model;

public class Tram extends Vehicle {

    public Tram(String id, int capacity) {
        super(id, capacity);
    }

    @Override
    public VehicleType getType() {
        return VehicleType.TRAM;
    }

    // Vitesse réduite à 0.5 : temps de parcours 2x plus long que le métro pour la même distance.
    @Override
    public double getSpeed() {
        return 0.5;
    }
}