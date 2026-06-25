package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class FastLadder extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Vertical climb speed on ladders and vines", 0.5, 0.2, 1.0));

    public FastLadder() {
        super("FastLadder", "Climb ladders and vines faster", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null) return;
        if (!mc.player.isClimbing()) return;

        Vec3d v = mc.player.getVelocity();
        double vy = mc.options.sneakKey.isPressed() ? -speed.get() : speed.get();
        if (mc.player.input.movementForward < 0) vy = -speed.get();

        mc.player.setVelocity(v.x, vy, v.z);
        mc.player.fallDistance = 0;
    }
}
