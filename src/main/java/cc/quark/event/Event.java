package cc.quark.event;

/**
 * Base class for all Quark events.
 * Cancellable events can be stopped from propagating further.
 */
public abstract class Event {

    private boolean cancelled = false;

    /**
     * Returns whether this event has been cancelled.
     *
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancel this event, preventing further processing by the game.
     * Only meaningful for cancellable events.
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Un-cancel this event.
     */
    public void uncancel() {
        this.cancelled = false;
    }

    /**
     * Returns whether this event supports being cancelled.
     * Subclasses may override to return false for non-cancellable events.
     *
     * @return true if the event can be cancelled
     */
    public boolean isCancellable() {
        return true;
    }
}
