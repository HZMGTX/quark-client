package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Scaffolddash - increases horizontal speed while sneaking on edges.
 */
public class Scaffolddash extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Sneak dash multiplier", 1.5, 1.0, 3.0));

    public Scaffolddash() {
        super("Scaffolddash", "Faster sneak bridging", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * speed.get(), v.y, v.z * speed.get());
    }
}
