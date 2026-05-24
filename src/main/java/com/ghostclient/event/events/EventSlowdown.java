package com.ghostclient.event.events;

import com.ghostclient.event.Event;

/**
 * Fired when the player would be slowed down (e.g., eating food, blocking with a shield,
 * using a bow, etc.). Cancel to prevent the slowdown multiplier from being applied.
 */
public class EventSlowdown extends Event {

    private float slowdownFactor;

    public EventSlowdown(float slowdownFactor) {
        this.slowdownFactor = slowdownFactor;
    }

    /**
     * The speed multiplier applied during the slowdown (e.g., 0.2f while eating).
     */
    public float getSlowdownFactor() {
        return slowdownFactor;
    }

    /**
     * Override the slowdown factor.  Set to 1.0 to negate the slowdown completely,
     * or any value between 0 and 1 to reduce it partially.
     */
    public void setSlowdownFactor(float slowdownFactor) {
        this.slowdownFactor = slowdownFactor;
    }
}
