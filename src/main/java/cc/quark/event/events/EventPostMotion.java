package cc.quark.event.events;

import cc.quark.event.Event;

/**
 * Fired after the client has sent its position / rotation packet to the server.
 * Modules can use this to restore spoofed values.
 */
public class EventPostMotion extends Event {

    @Override
    public boolean isCancellable() {
        return false;
    }
}
