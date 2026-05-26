package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Honey - removes the slowdown from honey blocks.
 */
public class Honey extends Module {

    public Honey() {
        super("Honey", "Ignore honey block slowdown", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos pos = mc.player.getBlockPos();
        boolean honey = mc.world.getBlockState(pos).isOf(Blocks.HONEY_BLOCK)
                || mc.world.getBlockState(pos.down()).isOf(Blocks.HONEY_BLOCK);
        if (!honey) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * 1.3, v.y, v.z * 1.3);
    }
}
