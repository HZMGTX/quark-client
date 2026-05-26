package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * DiagonalSpeed - extra boost when moving forward and sideways at once.
 */
public class DiagonalSpeed extends Module {

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Diagonal multiplier", 1.3, 1.0, 2.0));

    public DiagonalSpeed() {
        super("DiagonalSpeed", "Boosts diagonal movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean diagonal = mc.player.input.movementForward != 0
                && mc.player.input.movementSideways != 0;
        if (!diagonal || !mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * factor.get(), v.y, v.z * factor.get());
    }
}
