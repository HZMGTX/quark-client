package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

public class DoubleJump extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Upward velocity for the double jump", 0.42, 0.1, 1.5));

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown Ms", "Cooldown between double jumps in milliseconds", 1000, 200, 5000));

    private final TimerUtil cooldownTimer = new TimerUtil();
    private boolean usedDoubleJump = false;
    private boolean wasOnGround = false;

    public DoubleJump() {
        super("DoubleJump", "Allows a second jump while airborne", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        usedDoubleJump = false;
        wasOnGround = false;
    }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();

        // Reset double jump when on ground
        if (onGround) {
            usedDoubleJump = false;
            wasOnGround = true;
            return;
        }

        // In air: handle double jump
        if (!usedDoubleJump && wasOnGround) {
            // First jump: normal
            wasOnGround = false;
        } else if (!usedDoubleJump && !onGround) {
            // Second jump in air
            if (cooldownTimer.hasReached(cooldownMs.get())) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, boost.get(), vel.z);
                usedDoubleJump = true;
                cooldownTimer.reset();
                event.cancel(); // Cancel vanilla jump attempt in air
            }
        }
    }

    // Reset on landing via tick
    @EventHandler
    public void onTick(cc.quark.event.events.EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) {
            usedDoubleJump = false;
            wasOnGround = true;
        } else {
            wasOnGround = false;
        }
    }
}
