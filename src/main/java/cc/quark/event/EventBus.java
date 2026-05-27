package cc.quark.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central event bus for Quark.
 *
 * <p>Listeners register themselves via {@link #subscribe(Object)}.
 * Any public method annotated with {@link EventHandler} and taking exactly one
 * {@link Event} subclass parameter will be invoked when that event type is posted.
 *
 * <p>Thread safety: the internal maps are ConcurrentHashMap, so subscribing /
 * unsubscribing from other threads is safe.  Posting should only happen on the
 * main client thread.
 */
public class EventBus {

    /** Holds a listener method together with the owning object and its priority. */
    private static final class ListenerEntry {
        final Object owner;
        final MethodHandle handle;
        final int priority;

        ListenerEntry(Object owner, MethodHandle handle, int priority) {
            this.owner = owner;
            this.handle = handle;
            this.priority = priority;
        }
    }

    /** Map from event class â†’ ordered list of listener entries. */
    private final Map<Class<? extends Event>, List<ListenerEntry>> listenerMap =
            new ConcurrentHashMap<>();

    /** Lookup used to create MethodHandles from reflected Methods. */
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Register all {@link EventHandler}-annotated methods in {@code listener}
     * as subscribers.
     *
     * @param listener object to scan for listener methods
     */
    public void subscribe(Object listener) {
        if (listener == null) return;
        
        // Prevent duplicate subscriptions
        unsubscribe(listener);

        for (Method method : listener.getClass().getMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> paramType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(paramType)) continue;

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) paramType;

            int priority = method.getAnnotation(EventHandler.class).priority();

            try {
                MethodHandle handle = LOOKUP.unreflect(method).bindTo(listener);
                // Adapt the MethodHandle to accept Event as argument, avoiding WrongMethodTypeException
                // and eliminating the boxing overhead of invokeWithArguments()
                handle = handle.asType(java.lang.invoke.MethodType.methodType(void.class, Event.class));
                
                ListenerEntry entry = new ListenerEntry(listener, handle, priority);

                listenerMap.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(entry);

                // Keep list sorted by priority descending (highest first)
                listenerMap.get(eventClass).sort(
                        Comparator.comparingInt((ListenerEntry e) -> e.priority).reversed()
                );

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to create MethodHandle for " + method, e);
            }
        }
    }

    /**
     * Remove all listener methods belonging to {@code listener}.
     *
     * @param listener object to unregister
     */
    public void unsubscribe(Object listener) {
        if (listener == null) return;

        for (List<ListenerEntry> entries : listenerMap.values()) {
            Iterator<ListenerEntry> it = entries.iterator();
            while (it.hasNext()) {
                if (it.next().owner == listener) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Post an event to all registered subscribers.
     *
     * <p>Subscribers are invoked in priority order (highest priority first).
     * If the event is cancelled by any subscriber, posting stops unless the
     * remaining subscribers should still receive it (current implementation
     * stops on cancel for efficiency).
     *
     * @param event the event to post
     * @param <T>   event type
     * @return the event after all (or some) subscribers have processed it
     */
    public <T extends Event> T post(T event) {
        if (event == null) return null;

        List<ListenerEntry> entries = listenerMap.get(event.getClass());
        if (entries == null || entries.isEmpty()) return event;

        // Snapshot to avoid ConcurrentModificationException if a listener
        // subscribes / unsubscribes during dispatch
        List<ListenerEntry> snapshot = new ArrayList<>(entries);

        for (ListenerEntry entry : snapshot) {
            if (event.isCancellable() && event.isCancelled()) break;
            try {
                entry.handle.invokeExact(event);
            } catch (Throwable t) {
                // Log but do not propagate listener errors so other listeners
                // still get a chance to run
                System.err.println("[Quark EventBus] Exception in listener: " + t.getMessage());
                t.printStackTrace(System.err);
            }
        }

        return event;
    }

    /**
     * Returns an unmodifiable view of all registered listener entries for the
     * given event type (useful for debugging).
     *
     * @param eventClass event class to query
     * @return list of entries, possibly empty
     */
    public List<ListenerEntry> getListeners(Class<? extends Event> eventClass) {
        List<ListenerEntry> entries = listenerMap.get(eventClass);
        return entries == null ? Collections.emptyList() : Collections.unmodifiableList(entries);
    }

    /**
     * Remove all registered listeners (e.g., on client shutdown).
     */
    public void clear() {
        listenerMap.clear();
    }
}
