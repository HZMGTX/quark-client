package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NoSlowV extends Module {

    private final BoolSetting honeyBlock = register(new BoolSetting(
            "HoneyBlock", "Also cancel vertical slowdown from honey blocks", true));

    public NoSlowV() {
        super("NoSlowV", "Cancels vertical slowdown from soul sand and honey blocks", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Vec3d vel = mc.player.getVelocity();
        if (vel.y >= 0) return;

        BlockPos below = mc.player.getBlockPos().down();
        var blockBelow = mc.world.getBlockState(below).getBlock();

        // Soul sand pushes players down with bubble columns; cancel negative y
        if (blockBelow == Blocks.SOUL_SAND || blockBelow == Blocks.SOUL_SOIL) {
            mc.player.setVelocity(vel.x, 0, vel.z);
            return;
        }

        if (honeyBlock.isEnabled()) {
            BlockPos playerPos = mc.player.getBlockPos();
            var blockAt = mc.world.getBlockState(playerPos).getBlock();
            if (blockAt == Blocks.HONEY_BLOCK || blockBelow == Blocks.HONEY_BLOCK) {
                mc.player.setVelocity(vel.x, Math.max(vel.y, -0.05), vel.z);
            }
        }
    }
}
