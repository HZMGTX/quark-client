package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * ChestESP - renders ESP boxes around storage block entities.
 *
 * Color scheme:
 *   Chest        â†’ orange (1.0, 0.6, 0.1)
 *   Trapped Chest â†’ red   (1.0, 0.2, 0.2)
 *   Barrel       â†’ brown  (0.6, 0.4, 0.1)
 *   Hopper       â†’ gray   (0.6, 0.6, 0.6)
 *   Dropper      â†’ yellow (1.0, 1.0, 0.2)
 *   Dispenser    â†’ cyan   (0.2, 0.8, 1.0)
 *   Furnace      â†’ silver (0.8, 0.8, 0.8)
 *   Shulker Box  â†’ purple (0.7, 0.2, 1.0)
 */
public class ChestESP extends Module {

    private static final int RENDER_DISTANCE = 50; // blocks

    public ChestESP() {
        super("ChestESP", "Draws ESP boxes around storage containers", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();

        for (BlockEntity be : mc.world.blockEntities) {
            BlockPos pos = be.getPos();
            if (playerPos.getManhattanDistance(pos) > RENDER_DISTANCE) continue;

            float r, g, b;

            if (be instanceof ChestBlockEntity) {
                BlockState state = mc.world.getBlockState(pos);
                if (state.getBlock() instanceof TrappedChestBlock) {
                    r = 1.0f; g = 0.2f; b = 0.2f;
                } else {
                    r = 1.0f; g = 0.6f; b = 0.1f;
                }
            } else if (be instanceof BarrelBlockEntity) {
                r = 0.6f; g = 0.4f; b = 0.1f;
            } else if (be instanceof HopperBlockEntity) {
                r = 0.6f; g = 0.6f; b = 0.6f;
            } else if (be instanceof DropperBlockEntity) {
                r = 1.0f; g = 1.0f; b = 0.2f;
            } else if (be instanceof DispenserBlockEntity) {
                r = 0.2f; g = 0.8f; b = 1.0f;
            } else if (be instanceof FurnaceBlockEntity || be instanceof BlastFurnaceBlockEntity
                    || be instanceof SmokerBlockEntity) {
                r = 0.8f; g = 0.8f; b = 0.8f;
            } else if (be instanceof ShulkerBoxBlockEntity) {
                r = 0.7f; g = 0.2f; b = 1.0f;
            } else {
                continue;
            }

            Box box = new Box(pos);
            RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 0.85f, 1.5f);
            // Also draw a faint filled box for better visibility
            RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, 0.08f);
        }
    }
}
