package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class GravShift extends Module {

    private final DoubleSetting strength = register(new DoubleSetting(
            "Strength", "Gravity shift strength (negative = reduced gravity)", 0.5, 0.0, 1.0));

    public GravShift() {
        super("GravShift", "Shifts perceived gravity for climbing", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        Vec3d vel = mc.player.getVelocity();
        // Counteract gravity by adding upward velocity proportional to strength
        double counterGrav = 0.08 * strength.get(); // vanilla gravity = 0.08/tick
        mc.player.setVelocity(vel.x, vel.y + counterGrav, vel.z);
    }
}
