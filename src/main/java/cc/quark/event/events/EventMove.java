package cc.quark.event.events;

import cc.quark.event.Event;

/**
 * Fired when the player entity moves.
 * Modules can modify x, y, z to override movement velocity.
 */
public class EventMove extends Event {

    private double x;
    private double y;
    private double z;

    public EventMove(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
