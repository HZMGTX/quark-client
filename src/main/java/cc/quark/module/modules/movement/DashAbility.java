package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

public class DashAbility extends Module {

    private final DoubleSetting power = register(new DoubleSetting(
            "Power", "Dash velocity boost", 1.5, 0.5, 5.0));

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown Ms", "Cooldown between dashes in milliseconds", 2000, 200, 10000));

    private final TimerUtil cooldownTimer = new TimerUtil();
    private boolean dashPressed = false;

    public DashAbility() {
        super("DashAbility", "Short-range dash in movement direction", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        cooldownTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean jumpNow = mc.options.jumpKey.isPressed();

        // Detect fresh key press (rising edge)
        if (jumpNow && !dashPressed) {
            if (cooldownTimer.hasReached(cooldownMs.get())) {
                performDash();
                cooldownTimer.reset();
            }
        }

        dashPressed = jumpNow;
    }

    private void performDash() {
        if (mc.player == null) return;

        float yaw = mc.player.getYaw();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(yaw);

        double dx, dz;
        if (fwd == 0 && side == 0) {
            // Dash in look direction
            dx = -Math.sin(yawRad);
            dz = Math.cos(yawRad);
        } else {
            double len = Math.sqrt(fwd * fwd + side * side);
            dx = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len));
            dz = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len));
        }

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(dx * power.get(), vel.y, dz * power.get());
        mc.player.setSprinting(true);
    }

    public long getCooldownRemaining() {
        long elapsed = cooldownTimer.getTime();
        return Math.max(0, cooldownMs.get() - elapsed);
    }
}
