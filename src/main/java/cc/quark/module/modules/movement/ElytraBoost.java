package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * ElytraBoost - adds forward thrust in the look direction while gliding with an
 * elytra and holding the jump key.
 */
public class ElytraBoost extends Module {

    private final DoubleSetting power = register(new DoubleSetting(
            "Power", "Forward thrust per tick", 0.1, 0.0, 0.5));

    public ElytraBoost() {
        super("ElytraBoost", "Boosts elytra flight", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isFallFlying()) return;
        if (!mc.options.jumpKey.isPressed()) return;
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x + x * power.get(), v.y + y * power.get(), v.z + z * power.get());
    }
}
