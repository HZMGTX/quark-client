package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.Vec3d;

public class AutoRoll extends Module {

    private final IntSetting rollDistance = register(new IntSetting(
            "Roll Distance", "Distance in blocks to roll away from attacker", 3, 1, 8));

    private boolean wasBeingHurt = false;

    public AutoRoll() {
        super("AutoRoll", "Rolls away from incoming attacks to reduce damage", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasBeingHurt = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean beingHurt = mc.player.hurtTime > 0;

        // Trigger roll on the first tick of a hurt event
        if (beingHurt && !wasBeingHurt) {
            performRoll();
        }

        wasBeingHurt = beingHurt;
    }

    private void performRoll() {
        if (mc.player == null) return;

        Vec3d vel = mc.player.getVelocity();
        double d = rollDistance.get() * 0.18;

        // Roll away: move opposite to incoming knockback direction
        double kbX = vel.x;
        double kbZ = vel.z;
        double kbLen = Math.sqrt(kbX * kbX + kbZ * kbZ);

        double rollX, rollZ;
        if (kbLen > 0.01) {
            // Roll opposite to knockback
            rollX = -(kbX / kbLen) * d;
            rollZ = -(kbZ / kbLen) * d;
        } else {
            // No knockback direction — roll backward relative to player facing
            float yaw = mc.player.getYaw();
            double yawRad = Math.toRadians(yaw);
            rollX = Math.sin(yawRad) * d;
            rollZ = -Math.cos(yawRad) * d;
        }

        mc.player.setVelocity(rollX, mc.player.isOnGround() ? 0.3 : vel.y, rollZ);
        mc.player.fallDistance = 0;
    }
}
