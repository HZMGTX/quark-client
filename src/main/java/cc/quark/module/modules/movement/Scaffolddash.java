package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

/**
 * Scaffolddash - while sneaking (scaffold placement), burst speed in the
 * current look direction. Speed and Cooldown settings control the burst.
 */
public class Scaffolddash extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Burst speed multiplier", 1.8, 1.0, 4.0));
    private final IntSetting cooldown = register(new IntSetting(
            "Cooldown", "Ticks between bursts", 5, 1, 20));

    private final TimerUtil timer = new TimerUtil();
    private int cooldownTicks = 0;

    public Scaffolddash() {
        super("Scaffolddash", "Burst speed in look direction while sneaking for fast scaffold bridging", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        cooldownTicks = 0;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        double yawRad = Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double normFwd  = fwd  / len;
        double normSide = side / len;
        double s = 0.26 * speed.get();

        double bx = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * s;
        double bz = ( Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * s;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(bx, v.y, bz);

        cooldownTicks = cooldown.get();
    }
}
