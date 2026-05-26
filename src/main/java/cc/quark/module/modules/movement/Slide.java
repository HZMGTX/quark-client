package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Slide - preserves horizontal momentum after the player stops giving input,
 * letting them glide to a smooth stop instead of halting instantly.
 */
public class Slide extends Module {

    private final DoubleSetting friction = register(new DoubleSetting(
            "Friction", "Velocity retained per tick when idle", 0.92, 0.5, 0.99));

    public Slide() {
        super("Slide", "Smooth momentum on stop", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0) return;
        if (!mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * friction.get(), v.y, v.z * friction.get());
    }
}
