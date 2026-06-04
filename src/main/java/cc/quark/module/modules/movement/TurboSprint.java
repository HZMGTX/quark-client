package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class TurboSprint extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Speed multiplier applied during sprint", 1.4, 1.0, 5.0));

    private final BoolSetting fovEffect = register(new BoolSetting(
            "FOV Effect", "Allow the game's speed-related FOV change to show", true));

    public TurboSprint() {
        super("TurboSprint", "Extreme sprint speed with side effects", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Force sprint
        mc.player.setSprinting(true);

        // Only boost when moving
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double len = Math.sqrt(fwd * fwd + side * side);
        double dx = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len));
        double dz = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len));

        // Base sprint speed in Minecraft is ~0.287 blocks/tick
        double targetSpeed = 0.287 * multiplier.get();
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(dx * targetSpeed, vel.y, dz * targetSpeed);

        // Reset fall distance to avoid fall damage from fast horizontal movement
        if (!mc.player.isOnGround()) {
            mc.player.fallDistance = Math.max(0, mc.player.fallDistance - 0.1f);
        }
    }
}
