package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.Vec3d;

/**
 * HopStep - every N ticks hops (0.42 Y) and applies a forward burst; a combo
 * counter tracks consecutive hops and builds speed; getSuffix shows combo.
 */
public class HopStep extends Module {

    private final IntSetting interval = register(new IntSetting("Interval", "Ticks between hops", 8, 2, 20));
    private final IntSetting maxCombo = register(new IntSetting("Max Combo", "Maximum combo level", 5, 1, 10));

    private int tickCounter = 0;
    private int combo = 0;
    private boolean wasOnGround = false;

    public HopStep() {
        super("HopStep", "Periodic jumps with building combo speed boost", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        combo = 0;
        wasOnGround = false;
    }

    @Override
    public String getSuffix() {
        return combo > 0 ? "x" + combo : null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean onGround = mc.player.isOnGround();

        // Detect landing to continue or reset combo
        if (onGround && !wasOnGround) {
            // stayed alive on ground — combo continues
        } else if (!onGround && wasOnGround) {
            // just left ground by hop — increment combo
            combo = Math.min(combo + 1, maxCombo.get());
        }
        wasOnGround = onGround;

        if (!onGround) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) {
            combo = 0;
            tickCounter = 0;
            return;
        }

        tickCounter++;
        if (tickCounter < interval.get()) return;
        tickCounter = 0;

        // Compute direction-aligned burst
        double yawRad = Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double normFwd  = fwd  / len;
        double normSide = side / len;

        double burstBase = 0.28 + combo * 0.04;
        double bx = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * burstBase;
        double bz = ( Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * burstBase;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x + bx, 0.42, v.z + bz);
    }
}
