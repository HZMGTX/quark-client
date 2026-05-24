package cc.quark.util;

public class TimerUtil {

    private long lastMS = System.currentTimeMillis();

    public boolean hasReached(double ms) {
        return System.currentTimeMillis() - lastMS >= ms;
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public long getLastMS() {
        return lastMS;
    }

    public static TimerUtil create() {
        return new TimerUtil();
    }
}
