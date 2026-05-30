package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PathFinder extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Movement speed toward target", 0.3, 0.1, 1.0));
    private final BoolSetting jumpObstacles = register(new BoolSetting("JumpObstacles", "Auto-jump when colliding horizontally", true));

    private BlockPos target = null;

    public PathFinder() {
        super("PathFinder", "Moves player toward a target position automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        // Use the player's current crosshair look target as the destination
        var hitResult = mc.player.raycast(64.0, 1.0f, false);
        if (hitResult != null) {
            target = BlockPos.ofFloored(hitResult.getPos());
        } else {
            // Fall back to 10 blocks in front of player
            Vec3d look = mc.player.getRotationVec(1.0f);
            Vec3d pos = mc.player.getPos().add(look.multiply(10.0));
            target = BlockPos.ofFloored(pos);
        }
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (target == null) {
            setEnabled(false);
            return;
        }

        double dx = target.getX() + 0.5 - mc.player.getX();
        double dz = target.getZ() + 0.5 - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        // Disable when within 1 block of target
        if (dist < 1.0) {
            setEnabled(false);
            return;
        }

        double spd = speed.get();
        double nx = (dx / dist) * spd;
        double nz = (dz / dist) * spd;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(nx, vel.y, nz);

        // Jump if horizontally blocked
        if (jumpObstacles.isEnabled() && mc.player.isOnGround() && mc.player.horizontalCollision) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
        }
    }
}
