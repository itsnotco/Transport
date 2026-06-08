package org.example.factory;

import org.example.model.Metro;
import org.example.model.Train;
import org.example.model.Tram;
import org.example.model.Vehicle;
import org.example.model.VehicleType;

public class VehicleFactory {

    private static int counter = 0;

    public static Vehicle create(VehicleType type, int capacity) {
        counter++;
        String id = type.name().charAt(0) + String.valueOf(counter);

        if (type == VehicleType.TRAIN) return new Train(id, capacity);
        if (type == VehicleType.METRO) return new Metro(id, capacity);
        if (type == VehicleType.TRAM)  return new Tram(id, capacity);

        throw new IllegalArgumentException("Type inconnu : " + type);
    }
}