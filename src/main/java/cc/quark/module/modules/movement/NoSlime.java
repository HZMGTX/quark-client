package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * NoSlime - keeps full speed when standing on slime blocks.
 */
public class NoSlime extends Module {

    public NoSlime() {
        super("NoSlime", "Ignore slime block slowdown", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isOf(Blocks.SLIME_BLOCK)) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * 1.4, v.y, v.z * 1.4);
    }
}
