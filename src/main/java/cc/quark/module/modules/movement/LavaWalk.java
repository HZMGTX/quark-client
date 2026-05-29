package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * LavaWalk - while in lava applies a small upward velocity to keep the player
 * afloat and prevent sinking. Sneak to descend.
 */
public class LavaWalk extends Module {

    private final DoubleSetting floatForce = register(new DoubleSetting(
            "Float Force", "Upward velocity applied in lava", 0.1, 0.02, 0.5));

    public LavaWalk() {
        super("LavaWalk", "Float on top of lava; Sneak to sink", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isInLava()) return;
        if (mc.player.isSneaking()) return;

        double vy = mc.player.getVelocity().y;

        // Only push up if sinking or hovering at surface
        if (vy < floatForce.get()) {
            mc.player.setVelocity(
                    mc.player.getVelocity().x,
                    floatForce.get(),
                    mc.player.getVelocity().z);
        }
        mc.player.fallDistance = 0;
    }
}
