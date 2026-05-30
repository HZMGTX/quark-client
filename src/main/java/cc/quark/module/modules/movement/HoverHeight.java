package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * HoverHeight - maintains a specific height above the ground by raycasting
 * downward and adjusting vertical velocity. Applies upward velocity when too
 * low, gentle downward velocity when too high.
 */
public class HoverHeight extends Module {

    private final DoubleSetting targetHeight = register(new DoubleSetting(
            "TargetHeight", "Target height to maintain above ground (blocks)", 2.0, 0.5, 10.0));
    private final BoolSetting strictMode = register(new BoolSetting(
            "StrictMode", "Aggressively correct height deviation each tick", false));

    public HoverHeight() {
        super("HoverHeight", "Maintains a target height above the ground automatically", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) mc.player.fallDistance = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) return;

        double groundDist = getGroundDistance();
        if (groundDist < 0) return; // Could not find ground within range

        double target = targetHeight.get();
        double diff = groundDist - target;

        Vec3d vel = mc.player.getVelocity();
        double vy;

        if (strictMode.isEnabled()) {
            // Strictly try to match target height
            vy = diff * 0.1;
            vy = Math.max(-0.5, Math.min(0.5, vy));
        } else {
            if (diff < -0.3) {
                // Too low - rise
                vy = Math.min(0.15, (-diff) * 0.2);
            } else if (diff > 0.3) {
                // Too high - gentle fall
                vy = Math.max(-0.05, -diff * 0.05);
            } else {
                // Within tolerance - hover
                vy = 0;
            }
        }

        mc.player.setVelocity(vel.x, vy, vel.z);
        mc.player.fallDistance = 0;
    }

    /**
     * Raycasts downward from player position to find distance to ground.
     * Returns -1 if no ground found within search range.
     */
    private double getGroundDistance() {
        double playerY = mc.player.getY();
        double maxSearch = targetHeight.get() + 5.0;

        for (double dy = 0; dy <= maxSearch; dy += 0.5) {
            BlockPos checkPos = new BlockPos(
                    (int) Math.floor(mc.player.getX()),
                    (int) Math.floor(playerY - dy),
                    (int) Math.floor(mc.player.getZ()));
            if (!mc.world.getBlockState(checkPos).isAir()) {
                return dy;
            }
        }
        return -1;
    }
}
