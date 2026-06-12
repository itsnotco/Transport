/*
 * Train : le véhicule le plus rapide (2.5), dédié aux axes interurbains longue distance.
 * Dessert les gares de grande capacité.
 */
package model;

public class Train extends Vehicle {

    public Train(String id, int capacity) {
        super(id, capacity);
    }

    @Override
    public VehicleType getType() {
        return VehicleType.TRAIN;
    }

    // Vitesse 2.5x supérieure au métro, adaptée aux longues distances inter-stations.
    @Override
    public double getSpeed() {
        return 2.5;
    }
}