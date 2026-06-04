package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * DolphinDash - boosts the player's horizontal speed while swimming in water,
 * mimicking the dash-like momentum of a dolphin.
 */
public class DolphinDash extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal speed boost while in water (blocks/tick)", 1.3, 0.1, 5.0));

    private final BoolSetting onlySwimming = register(new BoolSetting(
            "Only Swimming", "Only apply boost when actively swimming (not just touching water)", true));

    public DolphinDash() {
        super("DolphinDash", "Boosts speed while in water like a dolphin", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        if (onlySwimming.isEnabled() && !mc.player.isSwimming()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double spd    = speed.get();

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd;
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(dx, vel.y, dz);
    }
}
