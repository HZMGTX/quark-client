package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class BlockOverlay extends Module {

    public BlockOverlay() {
        super("BlockOverlay", "Tints the screen based on the block the camera is inside", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        BlockPos camPos = BlockPos.ofFloored(mc.gameRenderer.getCamera().getPos());
        var block = mc.world.getBlockState(camPos).getBlock();

        int tint;
        if (block == Blocks.WATER) {
            tint = 0x44004488;
        } else if (block == Blocks.LAVA) {
            tint = 0x88CC4400;
        } else if (block == Blocks.POWDER_SNOW) {
            tint = 0x55AADDFF;
        } else {
            return;
        }

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        ctx.fill(0, 0, sw, sh, tint);
    }
}
