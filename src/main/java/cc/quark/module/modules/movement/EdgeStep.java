package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * EdgeStep - stops the player at block edges when sneaking to avoid falling.
 */
public class EdgeStep extends Module {

    public EdgeStep() {
        super("EdgeStep", "Stops at edges when sneaking", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isSneaking() || !mc.player.isOnGround()) return;
        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).isAir()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * 0.2, v.y, v.z * 0.2);
        }
    }
}
