package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class FastSwim extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Swim speed multiplier", 2.5, 1.0, 8.0));
    private final BoolSetting upBoost = register(new BoolSetting(
            "Up Boost", "Boost vertical speed too", true));
    private final BoolSetting inLava = register(new BoolSetting(
            "Lava", "Also speed in lava", false));

    public FastSwim() {
        super("FastSwim", "Move faster through water (and optionally lava)", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean inWater = mc.player.isTouchingWater();
        boolean inLavaFluid = mc.player.isInLava();
        if (!inWater && !(inLava.isEnabled() && inLavaFluid)) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        float yaw    = mc.player.getYaw();
        float fwd    = mc.player.input.movementForward;
        float side   = mc.player.input.movementSideways;
        double rad   = Math.toRadians(yaw);
        double spd   = speed.get() * 0.045;

        double nx = -Math.sin(rad) * fwd * spd + Math.cos(rad) * side * spd;
        double nz =  Math.cos(rad) * fwd * spd + Math.sin(rad) * side * spd;

        Vec3d vel = mc.player.getVelocity();
        double ny = upBoost.isEnabled() ? vel.y * speed.get() * 0.5 : vel.y;
        mc.player.setVelocity(nx, ny, nz);
    }
}
