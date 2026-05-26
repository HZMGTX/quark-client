package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Momentum - gradually accelerates horizontal speed the longer the player keeps
 * moving in the same direction.
 */
public class Momentum extends Module {

    private final DoubleSetting gain = register(new DoubleSetting(
            "Gain", "Speed gained per tick while moving", 1.02, 1.0, 1.1));
    private final DoubleSetting max = register(new DoubleSetting(
            "Max", "Maximum horizontal speed", 0.5, 0.3, 1.5));

    public Momentum() {
        super("Momentum", "Builds up speed while moving", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;
        Vec3d v = mc.player.getVelocity();
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
        if (horiz <= 0) return;
        double target = Math.min(horiz * gain.get(), max.get());
        double scale = target / horiz;
        mc.player.setVelocity(v.x * scale, v.y, v.z * scale);
    }
}
