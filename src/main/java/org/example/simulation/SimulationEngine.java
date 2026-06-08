package org.example.simulation;

import org.example.display.AsciiDisplay;
import org.example.network.NetworkGraph;
import org.example.observer.SimulationObservable;
import org.example.observer.SimulationObserver;

public class SimulationEngine {

    private static final int TICK_MINUTES = 5;
    private static final int TICK_DELAY_MS = 1000;

    private TimeManager timeManager;
    private Scheduler scheduler;
    private AsciiDisplay display;
    private NetworkGraph graph;
    private SimulationObservable observable;
    private boolean running;

    public SimulationEngine(NetworkGraph graph, Scheduler scheduler, AsciiDisplay display, SimulationObservable observable) {
        this.timeManager = TimeManager.getInstance();
        this.scheduler = scheduler;
        this.display = display;
        this.graph = graph;
        this.observable = observable;
        this.running = false;
    }

    public void start() {
        running = true;
        System.out.println("=== SIMULATION DÉMARRÉE ===\n");

        while (running) {
            scheduler.tick(TICK_MINUTES);
            timeManager.tick(TICK_MINUTES);
            display.printStatus(graph, scheduler.getVehicles(), timeManager.getFormattedTime());

            try {
                Thread.sleep(TICK_DELAY_MS);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stop() {
        running = false;
        System.out.println("=== SIMULATION ARRÊTÉE ===");
    }
}