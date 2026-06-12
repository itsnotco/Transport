/*
 * Énumération des états possibles d'un passager au cours de son trajet.
 * WAITING : en attente sur le quai.
 * ON_BOARD : embarqué dans un véhicule.
 * ARRIVED : a atteint sa destination finale.
 */
package model;

public enum PassengerState {
    WAITING,
    ON_BOARD,
    ARRIVED
}