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

    public String getFormattedTime() {
        int hours   = (currentMinute / 60) % 24;
        int minutes = currentMinute % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}