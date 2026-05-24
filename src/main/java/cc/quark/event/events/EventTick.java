package cc.quark.event.events;

import cc.quark.event.Event;

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
