package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class ShulkerViewer extends Module {

    public ShulkerViewer() {
        super("ShulkerViewer", "Shows the contents of a shulker box when you look at it", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        BlockEntity be = mc.world.getBlockEntity(pos);
        if (!(be instanceof ShulkerBoxBlockEntity shulker)) return;

        DrawContext ctx  = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int cols = 9, rows = 3;
        int slotSize = 18;
        int panelW = cols * slotSize + 8;
        int panelH = rows * slotSize + 8;
        int startX = (screenW - panelW) / 2;
        int startY = screenH / 2 + 20;

        ctx.fill(startX - 1, startY - 1, startX + panelW + 1, startY + panelH + 1, 0xFF444444);
        ctx.fill(startX, startY, startX + panelW, startY + panelH, 0xFF222222);

        for (int i = 0; i < shulker.size(); i++) {
            ItemStack stack = shulker.getStack(i);
            int col = i % cols;
            int row = i / cols;
            int ix = startX + 4 + col * slotSize;
            int iy = startY + 4 + row * slotSize;

            ctx.fill(ix, iy, ix + 16, iy + 16, 0xFF333333);
            if (!stack.isEmpty()) {
                ctx.drawItem(stack, ix, iy);
                if (stack.getCount() > 1) {
                    ctx.drawText(mc.textRenderer, String.valueOf(stack.getCount()),
                            ix + 9, iy + 9, 0xFFFFFFFF, true);
                }
            }
        }
    }
}
