package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Velocity2 - reduces incoming knockback by scaling the player's velocity
 * down each tick while taking damage.
 */
public class Velocity2 extends Module {

    private final IntSetting horizontal = register(new IntSetting(
            "Horizontal", "Horizontal knockback percent", 0, 0, 100));
    private final IntSetting vertical = register(new IntSetting(
            "Vertical", "Vertical knockback percent", 0, 0, 100));

    public Velocity2() {
        super("Velocity2", "Reduces incoming knockback", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hurtTime <= 0) return;
        Vec3d v = mc.player.getVelocity();
        double hx = v.x * (horizontal.get() / 100.0);
        double hz = v.z * (horizontal.get() / 100.0);
        double vy = v.y * (vertical.get() / 100.0);
        mc.player.setVelocity(hx, vy, hz);
    }
}
