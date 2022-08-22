package ga.elirey.locationexplorer.utils;

import lombok.Getter;

/**
 * Simple timer tool which can be used to count the number of milliseconds between
 * two points in time.
 * <p>
 * Note this class is by no means thread-safe, so a timer instance should only
 * be used for timing one specific task at a time, typically by a single thread.
 *
 * @author jthai
 * @author rweires
 * @author seustachi
 */
@Getter
public class CustomTimer {

    private long startTimeMilliseconds;

    /**
     * Creates a new timer that is reset at creation.
     */
    public CustomTimer() {
        this.startTimeMilliseconds = System.currentTimeMillis();
    }

    /**
     * Returns the time that elapsed since the last reset, in millis.
     */
    public long elapsedMsecs() {
        return System.currentTimeMillis() - this.startTimeMilliseconds;
    }

    /**
     * Returns the time that elapsed since the last reset, in seconds.
     */
    public double elapsedSecs() {
        return elapsedMsecs() / 1000.0;
    }

    /**
     * Sets the timer to the current time as base time.
     */
    public void resetTimer() {
        this.startTimeMilliseconds = System.currentTimeMillis();
    }

    public long now() {
        return System.currentTimeMillis();
    }
}

