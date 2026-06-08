package org.example.observer;

import java.util.ArrayList;

public class SimulationObservable {

    private ArrayList<SimulationObserver> observers;

    public SimulationObservable() {
        this.observers = new ArrayList<SimulationObserver>();
    }

    public void addObserver(SimulationObserver o) {
        observers.add(o);
    }

    public void notifyObservers(String message) {
        for (SimulationObserver o : observers) {
            o.onEvent(message);
        }
    }
}