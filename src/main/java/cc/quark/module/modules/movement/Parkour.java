package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;

public class Parkour extends Module {
    private final BoolSetting edgeJump = register(new BoolSetting("Edge Jump", "Auto-jump at block edges", true));

    public Parkour() { super("Parkour", "Auto-jumps at block edges for parkour", Category.MOVEMENT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        if (!edgeJump.isEnabled()) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking()) return;

        // Check if player is near edge of block
        double px = mc.player.getX();
        double pz = mc.player.getZ();
        double edgeDist = 0.22;
        boolean nearEdge = (px % 1 < edgeDist || px % 1 > 1 - edgeDist || pz % 1 < edgeDist || pz % 1 > 1 - edgeDist);

        if (nearEdge && (mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed())) {
            BlockPos below = BlockPos.ofFloored(px, mc.player.getY() - 0.1, pz);
            BlockPos ahead = below.offset(mc.player.getHorizontalFacing());
            if (mc.world.getBlockState(ahead).isAir()) {
                mc.player.jump();
            }
        }
    }
}
