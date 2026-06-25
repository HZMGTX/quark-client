package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.Vec3d;

/**
 * BunnyHop2 - enhanced bunny hop that automatically jumps on every landing
 * and applies a small speed bonus each hop.
 *
 * <p>The {@code Interval} setting controls how many ticks must pass between
 * automatic jumps, preventing the client from spamming too fast. The
 * {@code Speed} multiplier is applied to horizontal velocity on each hop.
 */
public class BunnyHop2 extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal velocity multiplier per hop", 1.1, 1.0, 3.0));

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Minimum ticks between auto-jumps", 1, 1, 20));

    private int ticksSinceJump = 0;

    public BunnyHop2() {
        super("BunnyHop2", "Enhanced bunny hop with auto-jump", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        ticksSinceJump = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        ticksSinceJump++;

        if (!mc.player.isOnGround()) return;
        if (ticksSinceJump < interval.get()) return;

        // Auto-jump by setting upward velocity and applying horizontal boost
        Vec3d vel = mc.player.getVelocity();
        double hx = vel.x * speed.get();
        double hz = vel.z * speed.get();

        mc.player.setVelocity(hx, 0.42, hz);
        mc.player.setSprinting(true);
        ticksSinceJump = 0;
    }
}
