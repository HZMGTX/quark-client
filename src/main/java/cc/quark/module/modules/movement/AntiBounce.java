package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * AntiBounce - cancels the bounce from slime and bed blocks.
 */
public class AntiBounce extends Module {

    public AntiBounce() {
        super("AntiBounce", "Stops slime/bed bouncing", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).isOf(Blocks.SLIME_BLOCK)) {
            Vec3d v = mc.player.getVelocity();
            if (v.y > 0) mc.player.setVelocity(v.x, 0.0, v.z);
        }
    }
}
