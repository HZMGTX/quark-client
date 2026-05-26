package cc.quark.ghost;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ClickSimulator {

    public enum ClickMode {
        NORMAL,
        JITTER,
        BUTTERFLY,
        DRAG
    }

    private final HumanizationEngine humanizer;

    private long lastLeftClick  = 0;
    private long lastRightClick = 0;
    private long nextClickDue   = 0;

    private boolean butterflyToggle = false;

    private final Deque<Long> leftClickHistory  = new ArrayDeque<>();
    private final Deque<Long> rightClickHistory = new ArrayDeque<>();

    public ClickSimulator(HumanizationEngine humanizer) {
        this.humanizer = humanizer;
    }

    public long generateClickInterval(double minCps, double maxCps) {
        double meanCps   = (minCps + maxCps) / 2.0;
        double meanMs    = 1000.0 / meanCps;
        double sigma     = 0.25 + (maxCps - minCps) / maxCps * 0.15;
        long interval    = humanizer.generateLogNormalClickInterval(meanMs, sigma);
        long minInterval = (long)(1000.0 / maxCps);
        long maxInterval = (long)(1000.0 / minCps);
        return Math.max(minInterval, Math.min(maxInterval, interval));
    }

    public List<Long> generateDragClickPattern(int targetCps) {
        List<Long> pattern = new ArrayList<>();
        long baseInterval = 1000L / Math.max(1, targetCps);
        int burstSize = 2 + humanizer.getRandom().nextInt(3);
        for (int i = 0; i < burstSize; i++) {
            long jitter = (long) humanizer.nextGaussian(0.0, baseInterval * 0.1);
            pattern.add(Math.max(5L, baseInterval / burstSize + jitter));
        }
        long gapJitter = (long) humanizer.nextGaussian(0.0, 10.0);
        pattern.add(Math.max(baseInterval, baseInterval + gapJitter));
        return pattern;
    }

    public List<Long> generateButterflyPattern(double minCps, double maxCps) {
        List<Long> pattern = new ArrayList<>();
        long shortInterval = (long)(1000.0 / maxCps);
        long longInterval  = (long)(1000.0 / minCps);
        int count = 6 + humanizer.getRandom().nextInt(4);
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                long j = (long) humanizer.nextGaussian(0.0, shortInterval * 0.08);
                pattern.add(Math.max(5L, shortInterval + j));
            } else {
                long j = (long) humanizer.nextGaussian(0.0, longInterval * 0.08);
                pattern.add(Math.max(shortInterval + 5L, longInterval + j));
            }
        }
        return pattern;
    }

    public List<Long> generateJitterPattern(double minCps, double maxCps) {
        List<Long> pattern = new ArrayList<>();
        long meanInterval = (long)(1000.0 / ((minCps + maxCps) / 2.0));
        int count = 8 + humanizer.getRandom().nextInt(5);
        for (int i = 0; i < count; i++) {
            long jitter = (long) humanizer.nextGaussian(0.0, meanInterval * 0.3);
            long interval = meanInterval + jitter;
            long minInterval = (long)(1000.0 / maxCps);
            long maxInterval = (long)(1000.0 / minCps);
            pattern.add(Math.max(minInterval, Math.min(maxInterval, interval)));
        }
        return pattern;
    }

    public long generateInterval(ClickMode mode, double minCps, double maxCps) {
        return switch (mode) {
            case NORMAL -> generateClickInterval(minCps, maxCps);
            case JITTER -> {
                long base = generateClickInterval(minCps, maxCps);
                long jitter = (long) humanizer.nextGaussian(0.0, base * 0.25);
                long minI = (long)(1000.0 / maxCps);
                long maxI = (long)(1000.0 / minCps);
                yield Math.max(minI, Math.min(maxI, base + jitter));
            }
            case BUTTERFLY -> {
                butterflyToggle = !butterflyToggle;
                long shortI = (long)(1000.0 / maxCps);
                long longI  = (long)(1000.0 / minCps);
                long target = butterflyToggle ? shortI : longI;
                long j = (long) humanizer.nextGaussian(0.0, target * 0.08);
                long minI = (long)(1000.0 / maxCps);
                yield Math.max(minI, target + j);
            }
            case DRAG -> {
                long base = 1000L / (long) maxCps;
                long j = (long) humanizer.nextGaussian(0.0, base * 0.1);
                yield Math.max(5L, base / 3 + j);
            }
        };
    }

    public boolean shouldClick() {
        return System.currentTimeMillis() >= nextClickDue;
    }

    public void onClicked(ClickMode mode, double minCps, double maxCps) {
        long now = System.currentTimeMillis();
        long interval = generateInterval(mode, minCps, maxCps);
        nextClickDue = now + interval;
        lastLeftClick = now;
        leftClickHistory.addLast(now);
        pruneHistory(leftClickHistory, now);
    }

    public boolean canLeftClick(int targetCPS) {
        long now = System.currentTimeMillis();
        long minInterval = (long)(1000.0 / targetCPS);
        long jitter = (long) humanizer.nextGaussian(0.0, 20.0);
        return now - lastLeftClick >= minInterval + jitter;
    }

    public boolean canRightClick(int targetCPS) {
        long now = System.currentTimeMillis();
        long minInterval = (long)(1000.0 / targetCPS);
        return now - lastRightClick >= minInterval;
    }

    public void simulateLeftClick() {
        long now = System.currentTimeMillis();
        lastLeftClick = now;
        leftClickHistory.addLast(now);
        pruneHistory(leftClickHistory, now);
    }

    public void simulateRightClick() {
        long now = System.currentTimeMillis();
        lastRightClick = now;
        rightClickHistory.addLast(now);
        pruneHistory(rightClickHistory, now);
    }

    public double getCurrentCPS() {
        long now = System.currentTimeMillis();
        pruneHistory(leftClickHistory, now);
        return leftClickHistory.size();
    }

    private void pruneHistory(Deque<Long> history, long now) {
        while (!history.isEmpty() && now - history.peekFirst() > 1000L) {
            history.pollFirst();
        }
    }

    public long getLastLeftClick()  { return lastLeftClick; }
    public long getLastRightClick() { return lastRightClick; }
    public long getNextClickDue()   { return nextClickDue; }
}
