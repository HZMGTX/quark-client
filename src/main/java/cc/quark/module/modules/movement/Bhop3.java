package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Bhop3 - bunny hop implementation that spoof on-ground packets and applies a
 * horizontal speed boost each time the player lands.
 *
 * <p>Uses EventPreMotion to alternate the reported on-ground state so the server
 * believes the player is always hopping, and EventTick to actually launch the
 * player upward on landing.
 */
public class Bhop3 extends Module {

    private final DoubleSetting speedBoost = register(new DoubleSetting(
            "Speed Boost", "Horizontal velocity multiplier applied on landing", 1.35, 1.0, 2.0));
    private final BoolSetting autoJump = register(new BoolSetting(
            "Auto Jump", "Automatically jump on landing without pressing space", true));

    private boolean wasOnGround = false;
    private boolean spoofGround = false;

    public Bhop3() {
        super("Bhop3", "Bhop with ground-spoof packets and landing speed boost", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasOnGround = false;
        spoofGround = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        boolean moving   = mc.player.input.movementForward != 0
                        || mc.player.input.movementSideways != 0;

        // Apply speed boost and jump when landing
        if (onGround && !wasOnGround && moving) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * speedBoost.get(), v.y, v.z * speedBoost.get());
        }

        if (onGround && moving && autoJump.isEnabled()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.42, v.z);
            spoofGround = true;
        } else {
            spoofGround = false;
        }

        wasOnGround = onGround;
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        // Alternate on-ground reporting to server for bhop-style movement
        if (spoofGround) {
            event.setOnGround(false);
        }
    }
}
