package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.MathHelper;

/**
 * PitchFly - pitch < -30 → fly up proportionally to how much pitch exceeds the
 * threshold; pitch > 30 → fly down; horizontal velocity always derived from yaw
 * so the player strafes naturally while controlling altitude with look angle.
 */
public class PitchFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed multiplier", 0.6, 0.1, 3.0));
    private final DoubleSetting deadZone = register(new DoubleSetting(
            "Dead Zone", "Pitch degrees from level that produce no vertical motion", 30.0, 5.0, 60.0));

    public PitchFly() {
        super("PitchFly", "Fly with look direction: pitch controls altitude, yaw controls direction", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float pitch  = mc.player.getPitch();
        float yaw    = mc.player.getYaw();
        double s     = speed.get();
        double dead  = deadZone.get();

        float yawRad   = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        // Vertical: proportional to how far pitch is outside the dead zone
        double vy;
        if (pitch < -dead) {
            // Looking up — fly up
            vy = s * (-pitch - dead) / (90.0 - dead);
        } else if (pitch > dead) {
            // Looking down — fly down
            vy = -s * (pitch - dead) / (90.0 - dead);
        } else {
            vy = 0.0;
        }

        // Horizontal: always direction of yaw, scaled by cosine of pitch
        double hScale = MathHelper.cos(pitchRad);
        double mx = -MathHelper.sin(yawRad) * hScale * s;
        double mz =  MathHelper.cos(yawRad) * hScale * s;

        mc.player.setVelocity(mx, vy, mz);
        mc.player.fallDistance = 0;
    }
}
