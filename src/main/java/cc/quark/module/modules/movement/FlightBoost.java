package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/**
 * FlightBoost - boosts movement speed while flying in creative mode or while
 * gliding with an elytra.
 */
public class FlightBoost extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Speed multiplier applied while flying", 1.5, 1.0, 10.0));

    private final BoolSetting onlyElytra = register(new BoolSetting(
            "Only Elytra", "Only boost when gliding with elytra, not creative fly", false));

    public FlightBoost() {
        super("FlightBoost", "Boost speed while flying (creative/elytra)", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean elytraFlying = mc.player.isFallFlying();
        boolean creativeFlying = mc.player.getAbilities().flying;

        if (onlyElytra.isEnabled()) {
            if (!elytraFlying) return;
        } else {
            if (!elytraFlying && !creativeFlying) return;
        }

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        Vec3d vel = mc.player.getVelocity();
        double yawRad = Math.toRadians(mc.player.getYaw());
        double spd = boost.get() * 0.1;

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd;
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd;

        mc.player.setVelocity(vel.x + dx, vel.y, vel.z + dz);
    }
}
