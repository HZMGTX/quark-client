package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.ghost.GhostManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class LegitSpeed extends Module {

    private final DoubleSetting scale = register(new DoubleSetting(
            "Scale", "Fraction of the AC-safe limit to target (1.0 = use full limit)", 0.95, 0.5, 1.0));

    public LegitSpeed() {
        super("LegitSpeed", "Caps speed to the active anti-cheat profile's safe limit", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSprinting()) return;

        double maxSpeed = GhostManager.INSTANCE.getMaxSpeed() * scale.get();
        Vec3d vel = mc.player.getVelocity();
        double horizSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        if (horizSpeed > maxSpeed) {
            double factor = maxSpeed / horizSpeed;
            mc.player.setVelocity(vel.x * factor, vel.y, vel.z * factor);
        }
    }
}
