package cc.quark.event.events;

import cc.quark.event.Event;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Fired during the 3D world render pass.
 * Modules use this to draw ESP boxes, tracer lines, etc.
 */
public class EventRender3D extends Event {

    private final MatrixStack matrixStack;
    private final float tickDelta;

    public EventRender3D(MatrixStack matrixStack, float tickDelta) {
        this.matrixStack = matrixStack;
        this.tickDelta = tickDelta;
    }

    /**
     * The matrix stack for issuing 3D render calls.
     */
    public MatrixStack getMatrixStack() {
        return matrixStack;
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
