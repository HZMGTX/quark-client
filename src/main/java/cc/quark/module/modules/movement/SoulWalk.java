package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SoulWalk extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Speed boost on soul sand (multiplier)", 1.5, 1.0, 4.0));

    public SoulWalk() {
        super("SoulWalk", "Cancels soul sand slow and bubbles up in soul sand valleys",
                Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        var blockBelow = mc.world.getBlockState(below).getBlock();

        boolean onSoul = blockBelow == Blocks.SOUL_SAND || blockBelow == Blocks.SOUL_SOIL;
        if (!onSoul) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        Vec3d vel = mc.player.getVelocity();
        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double len = Math.sqrt(fwd * fwd + side * side);
        double nFwd = fwd / len;
        double nSide = side / len;

        double baseSpeed = 0.13 * boost.get();
        double dx = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide) * baseSpeed;
        double dz = (Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide) * baseSpeed;

        // Cancel the downward bubble column push from soul sand
        double dy = vel.y < -0.1 ? 0 : vel.y;

        mc.player.setVelocity(dx, dy, dz);
    }
}
