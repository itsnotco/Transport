/*
 * Gère l'horloge interne de la simulation.
 * Le temps est exprimé en minutes entières ; tick() avance d'une unité
 * à chaque pas déclenché par l'utilisateur via la touche ENTRÉE.
 */
package simulation;

public class TimeManager {

    private int currentMinute;

    public TimeManager() {
        this.currentMinute = 0;
    }

    public void tick() {
        currentMinute++;
    }

    public int getCurrentMinute() {
        return currentMinute;
    }

    // Formate le temps courant en "HH:MM" pour l'affichage dans le tableau de bord Swing.
    public String getFormattedTime() {
        int hours = (currentMinute / 60) % 24;
        int minutes = currentMinute % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}