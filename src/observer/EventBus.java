package observer;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private final List<SimulationObserver> observers = new ArrayList<>();

    public void subscribe(SimulationObserver observer) {
        observers.add(observer);
    }

    public void publish(SimulationEvent event) {
        for (SimulationObserver o : observers) {
            o.onEvent(event);
        }
    }
}