package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class ZeroGrav extends Module {

    private final DoubleSetting gravity = register(new DoubleSetting(
            "Gravity", "Gravity strength (0 = weightless)", 0.001, 0.0, 0.08));

    private final BoolSetting slowFall = register(new BoolSetting(
            "Slow Fall", "Drift down slowly instead of hovering", false));

    public ZeroGrav() {
        super("ZeroGrav", "Near-zero gravity movement mode", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.fallDistance = 0.0f;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        Vec3d vel = mc.player.getVelocity();
        double newY;

        if (slowFall.isEnabled()) {
            // Drift downward at gravity speed
            newY = Math.max(vel.y - gravity.get(), -0.05);
        } else {
            // Counter gravity almost entirely
            newY = vel.y + (0.08 - gravity.get()); // counteract default gravity 0.08
            newY = Math.max(-0.05, Math.min(0.05, newY));
        }

        mc.player.setVelocity(vel.x, newY, vel.z);
        mc.player.fallDistance = 0.0f;
    }
}
