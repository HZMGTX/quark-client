package com.ghostclient.event.events;

import com.ghostclient.event.Event;

/**
 * Fired when a keyboard key is pressed.
 * Use the GLFW key codes (e.g., GLFW.GLFW_KEY_RIGHT_SHIFT).
 */
public class EventKey extends Event {

    private final int keyCode;

    public EventKey(int keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * The GLFW key code of the pressed key.
     */
    public int getKeyCode() {
        return keyCode;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
