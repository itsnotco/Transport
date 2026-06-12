/*
 * Énumération des états opérationnels d'un véhicule.
 * DOCKED : à l'arrêt en station, prêt à embarquer/débarquer des passagers.
 * IN_TRANSIT : en mouvement entre deux stations.
 * OUT_OF_SERVICE : immobilisé suite à un incident.
 */
package model;

public enum VehicleState {
    DOCKED,
    IN_TRANSIT,
    OUT_OF_SERVICE
}