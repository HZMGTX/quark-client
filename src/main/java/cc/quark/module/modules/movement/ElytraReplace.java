package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * ElytraReplace - keeps a slow-falling effect active while gliding so a broken
 * elytra never causes a fatal drop, acting as a soft replacement safety net.
 */
public class ElytraReplace extends Module {

    public ElytraReplace() {
        super("ElytraReplace", "Safety net for elytra flight", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (mc.player.fallDistance > 3.0f) {
            mc.player.fallDistance = 0;
        }
    }
}
