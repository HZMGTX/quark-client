package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Webless - restores movement speed when stuck inside a cobweb.
 */
public class Webless extends Module {

    public Webless() {
        super("Webless", "Ignore cobweb slowdown", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos pos = mc.player.getBlockPos();
        if (!mc.world.getBlockState(pos).isOf(Blocks.COBWEB)) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * 5.0, v.y, v.z * 5.0);
    }
}
