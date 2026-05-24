package com.ghostclient.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking a method as an event listener.
 * The method must take exactly one parameter that extends {@link Event}.
 *
 * <p>Example usage:
 * <pre>
 *   {@literal @}EventHandler
 *   public void onTick(EventTick event) {
 *       // handle tick
 *   }
 * </pre>
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    /**
     * Optional priority for ordering listeners (higher = called first).
     */
    int priority() default 0;
}
