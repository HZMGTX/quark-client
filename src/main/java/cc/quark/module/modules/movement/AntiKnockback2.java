package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AntiKnockback2 - fully or partially negates incoming knockback.
 */
public class AntiKnockback2 extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Cancel strategy", "Full", "Full", "Half"));

    public AntiKnockback2() {
        super("AntiKnockback2", "Negates knockback", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hurtTime <= 0) return;
        Vec3d v = mc.player.getVelocity();
        double scale = mode.is("Half") ? 0.5 : 0.0;
        mc.player.setVelocity(v.x * scale, v.y, v.z * scale);
    }
}
