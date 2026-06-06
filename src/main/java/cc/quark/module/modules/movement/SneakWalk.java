package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SneakWalk - walks at full (or configurable) speed while the sneak key is held.
 *
 * Vanilla sneak caps horizontal movement to ~0.065 b/t. This module overrides
 * that by applying the sprint-walk velocity vector when the player is sneaking.
 */
public class SneakWalk extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Movement speed multiplier while sneaking (1.0 = sprint speed)", 1.0, 0.1, 2.0));

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Also force-sprint while sneaking", false));

    public SneakWalk() {
        super("SneakWalk", "Walks at full speed while holding the sneak key", Category.MOVEMENT);
    }

    @Override
    public void onEnable() { mc.getEventBus().subscribe(this); }

    @Override
    public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;
        if (!mc.player.isOnGround()) return;

        float movFwd  = mc.player.input.movementForward;
        float movSide = mc.player.input.movementSideways;

        if (movFwd == 0 && movSide == 0) return;

        if (sprint.isEnabled()) mc.player.setSprinting(true);

        double yaw   = Math.toRadians(mc.player.getYaw());
        double sin   = -Math.sin(yaw);
        double cos   =  Math.cos(yaw);

        // Sprint-speed horizontal base ~ 0.286 b/t
        double baseSpeed = 0.286 * speed.get();

        // Normalise input
        double len = Math.sqrt(movFwd * movFwd + movSide * movSide);
        double nFwd  = movFwd  / len;
        double nSide = movSide / len;

        double vx = (sin * nFwd + cos * nSide) * baseSpeed;
        double vz = (cos * nFwd - sin * nSide) * baseSpeed;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vx, vel.y, vz);
    }
}
