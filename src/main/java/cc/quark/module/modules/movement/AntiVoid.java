package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AntiVoid - saves the player from falling into the void.
 *
 * <p>While on the ground the last safe position is continuously updated.
 * When the player's Y drops below the configured threshold their velocity is
 * zeroed and they are teleported back to the last safe position.
 * A secondary threshold (threshold - 10) applies an emergency upward velocity
 * as a last resort.
 */
public class AntiVoid extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Y Threshold", "Y level at which anti-void activates", -10.0, -100.0, 10.0));

    private Vec3d lastSafePos = null;
    private boolean wasOnGround = false;

    public AntiVoid() {
        super("AntiVoid", "Teleport back to safety when falling into the void", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            lastSafePos = mc.player.getPos();
        }
        wasOnGround = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Vec3d pos = mc.player.getPos();

        // Update last safe position whenever the player is on solid ground
        if (mc.player.isOnGround() && pos.y > threshold.get()) {
            lastSafePos = pos;
            wasOnGround = true;
        }

        double limit = threshold.get();

        if (pos.y < limit) {
            if (lastSafePos != null) {
                // Teleport back to last safe spot
                mc.player.setPos(lastSafePos.x, lastSafePos.y, lastSafePos.z);
                mc.player.setVelocity(0, 0, 0);
                mc.player.fallDistance = 0;
            } else {
                // No safe position recorded — emergency upward boost
                mc.player.setVelocity(mc.player.getVelocity().x, 0.5, mc.player.getVelocity().z);
                mc.player.fallDistance = 0;
            }
        } else if (pos.y < limit + 5 && !mc.player.isOnGround()) {
            // Approaching threshold — stop downward velocity to give us a chance
            Vec3d vel = mc.player.getVelocity();
            if (vel.y < 0) {
                mc.player.setVelocity(vel.x, 0, vel.z);
            }
        }
    }
}
