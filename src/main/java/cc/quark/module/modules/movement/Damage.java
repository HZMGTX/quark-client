package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Damage - boosts the player's velocity in their look direction right after
 * taking damage, converting knockback into a controlled launch.
 */
public class Damage extends Module {

    private final DoubleSetting power = register(new DoubleSetting(
            "Power", "Launch multiplier on hit", 1.5, 1.0, 4.0));

    public Damage() {
        super("Damage", "Boosts velocity when hurt", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hurtTime <= 0) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * power.get(), v.y, v.z * power.get());
    }
}
