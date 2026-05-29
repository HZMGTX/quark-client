package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * Upward - hold Jump to fly straight up at Speed; reset fall distance to
 * prevent fall damage when descending after releasing Jump.
 */
public class Upward extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Upward speed (blocks/tick)", 0.5, 0.1, 2.0));

    public Upward() {
        super("Upward", "Hold Jump to ascend straight up; resets fall distance", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.options.jumpKey.isPressed()) return;
        if (mc.player.isOnGround()) return;

        mc.player.setVelocity(
                mc.player.getVelocity().x,
                speed.get(),
                mc.player.getVelocity().z);
        mc.player.fallDistance = 0;
    }
}
