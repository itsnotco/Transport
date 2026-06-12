/*
 * Métro : vitesse modérée (1.0), forte capacité.
 * Circule sur les lignes souterraines du réseau urbain.
 */
package model;

public class Metro extends Vehicle {

    public Metro(String id, int capacity) {
        super(id, capacity);
    }

    @Override
    public VehicleType getType() {
        return VehicleType.METRO;
    }

    // Vitesse normalisée à 1.0 unité de distance par minute de simulation.
    @Override
    public double getSpeed() {
        return 1.0;
    }
}