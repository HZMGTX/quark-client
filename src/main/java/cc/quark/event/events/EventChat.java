package cc.quark.event.events;

import cc.quark.event.Event;

public class EventChat extends Event {
    private String message;
    private final boolean incoming;

    public EventChat(String message, boolean incoming) {
        this.message = message;
        this.incoming = incoming;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isIncoming() { return incoming; }
}
