package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * TunnelBore - Mines a tunnel in the direction the player is facing.
 * Width x Height cross-section, advancing one layer at a time.
 */
public class TunnelBore extends Module {

    private final IntSetting width = register(new IntSetting(
            "Width", "Tunnel width in blocks", 1, 1, 5));
    private final IntSetting height = register(new IntSetting(
            "Height", "Tunnel height in blocks", 2, 1, 4));

    private final TimerUtil timer = new TimerUtil();

    public TunnelBore() {
        super("TunnelBore", "Auto-mines tunnels in straight lines", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(50)) return;
        timer.reset();

        Direction facing = mc.player.getHorizontalFacing();
        BlockPos origin = mc.player.getBlockPos();

        int w = width.get();
        int h = height.get();
        int halfW = w / 2;

        // Calculate perpendicular direction for width
        Direction perp = facing.rotateYClockwise();

        for (int dy = 0; dy < h; dy++) {
            for (int dw = -halfW; dw <= halfW; dw++) {
                BlockPos pos = origin
                        .offset(facing, 1)        // one block in front
                        .offset(perp, dw)         // sideways offset
                        .up(dy);                  // height offset

                if (!mc.world.getBlockState(pos).isAir()) {
                    mc.interactionManager.attackBlock(pos, facing.getOpposite());
                    mc.player.swingHand(Hand.MAIN_HAND);
                    return; // Mine one block per tick
                }
            }
        }

        // All blocks in front are air — advance the player
        mc.options.forwardKey.setPressed(true);
    }
}
