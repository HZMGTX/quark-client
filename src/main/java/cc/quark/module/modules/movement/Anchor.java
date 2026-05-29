package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * Anchor - zeroes all velocity every tick so the player is pinned to the spot.
 * Optionally allows a configurable maximum speed instead of a hard zero,
 * letting tiny legitimate movements pass while still preventing large bursts
 * from knockback or explosions.
 */
public class Anchor extends Module {

    private final BoolSetting hardStop = register(new BoolSetting(
            "Hard Stop", "Zero velocity completely each tick", true));
    private final DoubleSetting speedLimit = register(new DoubleSetting(
            "Speed Limit", "Max allowed speed when Hard Stop is off (blocks/tick)", 0.05, 0.0, 0.5));
    private final BoolSetting resetFall = register(new BoolSetting(
            "Reset Fall", "Keep fall distance at zero to prevent fall damage", true));

    public Anchor() {
        super("Anchor", "Lock position by zeroing velocity every tick", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        if (hardStop.isEnabled()) {
            event.setX(0);
            event.setY(0);
            event.setZ(0);
            mc.player.setVelocity(0, 0, 0);
        } else {
            double limit = speedLimit.get();
            double hLen  = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
            if (hLen > limit && hLen > 0) {
                double scale = limit / hLen;
                event.setX(event.getX() * scale);
                event.setZ(event.getZ() * scale);
            }
            // Always cancel vertical velocity to stop falling
            event.setY(0);
            mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
        }

        if (resetFall.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
