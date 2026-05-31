package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class VerticalFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Vertical movement speed (blocks/tick)", 0.3, 0.05, 2.0));

    public VerticalFly() {
        super("VerticalFly", "Fly mode that only moves vertically (up/down)", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        Vec3d vel = mc.player.getVelocity();
        double dy = 0;

        if (mc.options.jumpKey.isPressed()) {
            dy = speed.get();
        } else if (mc.options.sneakKey.isPressed()) {
            dy = -speed.get();
        }

        // Cancel gravity; zero horizontal velocity to enforce vertical-only movement
        mc.player.setVelocity(0, dy, 0);
        mc.player.fallDistance = 0;
    }
}
