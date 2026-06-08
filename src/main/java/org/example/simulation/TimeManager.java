package org.example.simulation;

public class TimeManager {

    private static TimeManager instance;
    private int currentMinute;

    private TimeManager() {
        this.currentMinute = 0;
    }

    public static TimeManager getInstance() {
        if (instance == null) {
            instance = new TimeManager();
        }
        return instance;
    }

    public void tick(int minutes) {
        currentMinute += minutes;
    }

    public int getCurrentMinute() {
        return currentMinute;
    }

    public String getFormattedTime() {
        int hours   = (currentMinute / 60) % 24;
        int minutes = currentMinute % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}