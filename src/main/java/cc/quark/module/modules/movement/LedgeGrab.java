package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * LedgeGrab - when the player's feet are at a ledge edge (block at foot level,
 * air below) and moving into it: zero horizontal+vertical velocity and pull up
 * 0.5 blocks smoothly.
 */
public class LedgeGrab extends Module {

    private final DoubleSetting pullStrength = register(new DoubleSetting(
            "Pull Strength", "Upward velocity applied when grabbing a ledge", 0.5, 0.2, 1.0));

    private boolean grabbing = false;

    public LedgeGrab() {
        super("LedgeGrab", "Grab ledge edges — zero velocity then pull up", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        grabbing = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Only trigger when airborne and moving forward/horizontally
        if (mc.player.isOnGround()) {
            grabbing = false;
            return;
        }

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        Vec3d vel = mc.player.getVelocity();
        // Only grab when falling (not jumping)
        if (vel.y > 0.05) return;

        // Check for a ledge: a solid block at eye level and air at player's
        // foot level two blocks in front
        BlockPos feetPos = mc.player.getBlockPos();
        // Check each cardinal direction for collision
        double yawRad = Math.toRadians(mc.player.getYaw());
        int nx = (int) Math.round(-Math.sin(yawRad));
        int nz = (int) Math.round( Math.cos(yawRad));

        BlockPos frontFoot = feetPos.add(nx, 0, nz);
        BlockPos frontHead = frontFoot.up();
        BlockPos aboveLedge = frontFoot.up(2);

        boolean solidFoot = !mc.world.getBlockState(frontFoot).isAir();
        boolean airHead   =  mc.world.getBlockState(frontHead).isAir();
        boolean airAbove  =  mc.world.getBlockState(aboveLedge).isAir();

        if (solidFoot && airHead && airAbove) {
            // Grab: cancel horizontal + vertical, then pull up
            mc.player.setVelocity(0.0, pullStrength.get(), 0.0);
            grabbing = true;
        }
    }
}
