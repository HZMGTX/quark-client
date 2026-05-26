package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * AntiTrap - bursts out of webs and powder snow that try to trap the player.
 */
public class AntiTrap extends Module {

    public AntiTrap() {
        super("AntiTrap", "Escape trapping blocks", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos pos = mc.player.getBlockPos();
        boolean trapped = mc.world.getBlockState(pos).isOf(Blocks.COBWEB)
                || mc.world.getBlockState(pos).isOf(Blocks.POWDER_SNOW);
        if (!trapped) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * 4.0, 0.42, v.z * 4.0);
    }
}
