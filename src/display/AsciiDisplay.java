package display;

import observer.SimulationEvent;
import observer.SimulationObserver;

public class AsciiDisplay implements SimulationObserver {

    @Override
    public void onEvent(SimulationEvent e) {
        if (e.getKind() == SimulationEvent.Kind.DEPARTURE) {
            System.out.println("  [" + e.getVehicle().getId() + "] depart "
                    + e.getFrom().getName() + " -> " + e.getTo().getName());
        } else {
            System.out.println("  [" + e.getVehicle().getId() + "] arrive a "
                    + e.getTo().getName());
        }
    }
}