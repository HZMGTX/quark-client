package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

public class CannonJump extends Module {

    private final DoubleSetting power = register(new DoubleSetting(
            "Power", "Launch power/velocity", 2.0, 0.5, 10.0));

    private final DoubleSetting angle = register(new DoubleSetting(
            "Angle", "Launch angle in degrees (45 = balanced)", 45.0, 5.0, 85.0));

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown Ms", "Cooldown between launches in milliseconds", 5000, 500, 30000));

    private final TimerUtil cooldownTimer = new TimerUtil();
    private boolean jumpPressed = false;

    public CannonJump() {
        super("CannonJump", "Launch forward like a cannon", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        cooldownTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Only fire when on ground
        if (!mc.player.isOnGround()) return;

        boolean jumpNow = mc.options.jumpKey.isPressed();

        // Rising edge detection with sneak held = cannon
        if (jumpNow && !jumpPressed && mc.options.sneakKey.isPressed()) {
            if (cooldownTimer.hasReached(cooldownMs.get())) {
                launch();
                cooldownTimer.reset();
            }
        }

        jumpPressed = jumpNow;
    }

    private void launch() {
        if (mc.player == null) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double angleRad = Math.toRadians(angle.get());

        double horizontal = power.get() * Math.cos(angleRad);
        double vertical = power.get() * Math.sin(angleRad);

        double dx = -Math.sin(yawRad) * horizontal;
        double dz = Math.cos(yawRad) * horizontal;

        mc.player.setVelocity(dx, vertical, dz);
        mc.player.setSprinting(true);
    }

    public long getCooldownRemaining() {
        return Math.max(0, cooldownMs.get() - cooldownTimer.getTime());
    }

    @Override
    public String getSuffix() {
        long rem = getCooldownRemaining();
        if (rem > 0) return String.format("%.1fs", rem / 1000.0);
        return "Ready";
    }
}
