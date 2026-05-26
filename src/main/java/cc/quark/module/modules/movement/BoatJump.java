package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * BoatJump - lets the player hop while riding so a boat can leap.
 */
public class BoatJump extends Module {

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Jump height", 0.5, 0.2, 1.2));

    public BoatJump() {
        super("BoatJump", "Hop while riding", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.hasVehicle()) return;
        if (!mc.options.jumpKey.isPressed()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, height.get(), v.z);
    }
}
