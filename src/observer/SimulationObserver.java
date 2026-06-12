/*
 * Interface du pattern Observer appliqué à la simulation.
 * Tout composant souhaitant réagir aux événements de mouvement (départs/arrivées)
 * doit implémenter cette interface et s'enregistrer auprès de l'EventBus.
 */
package observer;

public interface SimulationObserver {
    void onEvent(SimulationEvent event);
}