package com.ghostclient.event.events;

import com.ghostclient.event.Event;

/**
 * Fired every client tick (from the Minecraft game loop).
 * Not cancellable.
 */
public class EventTick extends Event {

    @Override
    public boolean isCancellable() {
        return false;
    }
}
