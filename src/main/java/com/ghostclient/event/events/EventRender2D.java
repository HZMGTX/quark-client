package com.ghostclient.event.events;

import com.ghostclient.event.Event;
import net.minecraft.client.gui.DrawContext;

/**
 * Fired during the 2D HUD render pass.
 * Modules use this to draw overlays, ESP labels, arrays, etc.
 */
public class EventRender2D extends Event {

    private final DrawContext drawContext;
    private final float tickDelta;

    public EventRender2D(DrawContext drawContext, float tickDelta) {
        this.drawContext = drawContext;
        this.tickDelta = tickDelta;
    }

    /**
     * The draw context for issuing 2D render calls.
     */
    public DrawContext getDrawContext() {
        return drawContext;
    }

    /**
     * Partial tick delta for smooth interpolation.
     */
    public float getTickDelta() {
        return tickDelta;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
