package software_analytics.group2.backend.utility;

/**
 * Class used to compute the cloning time of a git repository.
 */
public class Timer {

    private long startTime;
    private long stopTime;

    public Timer() {
        this.startTime = System.currentTimeMillis();
        this.stopTime = 0;
    }

    public void stopTimer() {
        this.stopTime = System.currentTimeMillis();
    }

    public long computeTotalTime() {
        return (this.stopTime - this.startTime);
    }
}
