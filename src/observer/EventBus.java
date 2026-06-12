/*
 * Bus d'événements central (pattern Publisher/Subscriber).
 * Les observateurs s'inscrivent via subscribe() et reçoivent tous les événements
 * publiés sans couplage direct avec la source émettrice (VehicleScheduler).
 */
package observer;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private final List<SimulationObserver> observers = new ArrayList<>();

    public void subscribe(SimulationObserver observer) {
        observers.add(observer);
    }

    // Diffuse l'événement à chaque observateur inscrit, dans l'ordre d'inscription.
    public void publish(SimulationEvent event) {
        for (SimulationObserver o : observers) {
            o.onEvent(event);
        }
    }
}