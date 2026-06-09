package model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Station {

    private String id;
    private String name;
    private int capacity;
    private List<VehicleType> supportedTypes;

    public Station(String id, String name, int capacity, VehicleType... supportedTypes) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.supportedTypes = Arrays.asList(supportedTypes);
    }

    public boolean supports(VehicleType type) {
        return supportedTypes.contains(type);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<VehicleType> getSupportedTypes() {
        return supportedTypes;
    }
}