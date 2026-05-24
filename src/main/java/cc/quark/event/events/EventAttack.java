package cc.quark.event.events;

import cc.quark.event.Event;
import net.minecraft.entity.Entity;

/**
 * Fired when the player attacks an entity.
 * Cancel to prevent the attack from occurring.
 */
public class EventAttack extends Event {

    private Entity target;

    public EventAttack(Entity target) {
        this.target = target;
    }

    /**
     * The entity being attacked.
     */
    public Entity getTarget() {
        return target;
    }

    /**
     * Change the attack target.
     */
    public void setTarget(Entity target) {
        this.target = target;
    }
}
