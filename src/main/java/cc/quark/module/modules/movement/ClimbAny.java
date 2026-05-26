package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * ClimbAny - lets the player climb up a wall they are pushing into.
 */
public class ClimbAny extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Climb speed", 0.2, 0.05, 0.5));

    public ClimbAny() {
        super("ClimbAny", "Climb any solid wall", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.input.jumping) return;
        Direction facing = mc.player.getHorizontalFacing();
        BlockPos front = mc.player.getBlockPos().offset(facing);
        if (mc.world.getBlockState(front).isAir()) return;
        if (mc.world.getBlockState(front).isOf(Blocks.AIR)) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, speed.get(), v.z);
    }
}
