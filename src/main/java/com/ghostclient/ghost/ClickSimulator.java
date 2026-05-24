package com.ghostclient.ghost;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ThreadLocalRandom;

public class ClickSimulator {

    private final HumanizationEngine humanizer;
    private long lastLeftClick = 0;
    private long lastRightClick = 0;
    private final Deque<Long> leftClickHistory = new ArrayDeque<>();
    private final Deque<Long> rightClickHistory = new ArrayDeque<>();

    public ClickSimulator(HumanizationEngine humanizer) {
        this.humanizer = humanizer;
    }

    public boolean canLeftClick(int targetCPS) {
        long now = System.currentTimeMillis();
        long minInterval = (long)(1000.0 / targetCPS);
        long jitter = (long)(ThreadLocalRandom.current().nextGaussian() * 20);
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
        // Prune entries older than 1 second
        while (!leftClickHistory.isEmpty() && now - leftClickHistory.peekFirst() > 1000L) {
            leftClickHistory.pollFirst();
        }
    }

    public void simulateRightClick() {
        long now = System.currentTimeMillis();
        lastRightClick = now;
        rightClickHistory.addLast(now);
        while (!rightClickHistory.isEmpty() && now - rightClickHistory.peekFirst() > 1000L) {
            rightClickHistory.pollFirst();
        }
    }

    public double getCurrentCPS() {
        long now = System.currentTimeMillis();
        while (!leftClickHistory.isEmpty() && now - leftClickHistory.peekFirst() > 1000L) {
            leftClickHistory.pollFirst();
        }
        return leftClickHistory.size();
    }

    public long getLastLeftClick()  { return lastLeftClick; }
    public long getLastRightClick() { return lastRightClick; }
}
