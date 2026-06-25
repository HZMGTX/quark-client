package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

/**
 * BlockInfo — shows information about the block your crosshair is targeting.
 * Displays block name, registry ID, position, light level, and hardness.
 */
public class BlockInfo extends Module {

    private final BoolSetting showName = register(new BoolSetting(
            "Name", "Show block display name", true));
    private final BoolSetting showId = register(new BoolSetting(
            "Registry ID", "Show block registry identifier", true));
    private final BoolSetting showPos = register(new BoolSetting(
            "Position", "Show block coordinates", true));
    private final BoolSetting showLight = register(new BoolSetting(
            "Light Level", "Show sky and block light at target", true));
    private final BoolSetting showHardness = register(new BoolSetting(
            "Hardness", "Show block hardness value", false));
    private final IntSetting xOffset = register(new IntSetting(
            "X", "HUD X position", 4, 0, 500));
    private final IntSetting yOffset = register(new IntSetting(
            "Y", "HUD Y position", 4, 0, 500));

    public BlockInfo() {
        super("BlockInfo", "Shows info about the block you are looking at on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        if (state.isAir()) return;

        Block block = state.getBlock();
        DrawContext ctx = event.getDrawContext();
        int x = xOffset.get();
        int y = yOffset.get();
        int lineH = 10;
        int color = 0xFFFFFFFF;
        int labelColor = 0xFFAAAAAA;

        if (showName.isEnabled()) {
            String name = block.getName().getString();
            ctx.drawTextWithShadow(mc.textRenderer, "Block: ", x, y, labelColor);
            ctx.drawTextWithShadow(mc.textRenderer, name, x + mc.textRenderer.getWidth("Block: "), y, color);
            y += lineH;
        }

        if (showId.isEnabled()) {
            String id = Registries.BLOCK.getId(block).toString();
            ctx.drawTextWithShadow(mc.textRenderer, "ID: ", x, y, labelColor);
            ctx.drawTextWithShadow(mc.textRenderer, id, x + mc.textRenderer.getWidth("ID: "), y, color);
            y += lineH;
        }

        if (showPos.isEnabled()) {
            String posStr = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
            ctx.drawTextWithShadow(mc.textRenderer, "Pos: ", x, y, labelColor);
            ctx.drawTextWithShadow(mc.textRenderer, posStr, x + mc.textRenderer.getWidth("Pos: "), y, color);
            y += lineH;
        }

        if (showLight.isEnabled()) {
            int blockLight = mc.world.getLightLevel(LightType.BLOCK, pos);
            int skyLight = mc.world.getLightLevel(LightType.SKY, pos);
            String lightStr = "B:" + blockLight + " S:" + skyLight;
            ctx.drawTextWithShadow(mc.textRenderer, "Light: ", x, y, labelColor);
            ctx.drawTextWithShadow(mc.textRenderer, lightStr, x + mc.textRenderer.getWidth("Light: "), y, color);
            y += lineH;
        }

        if (showHardness.isEnabled()) {
            float hardness = state.getHardness(mc.world, pos);
            String hardStr = hardness < 0 ? "Unbreakable" : String.format("%.1f", hardness);
            ctx.drawTextWithShadow(mc.textRenderer, "Hardness: ", x, y, labelColor);
            ctx.drawTextWithShadow(mc.textRenderer, hardStr, x + mc.textRenderer.getWidth("Hardness: "), y, color);
        }
    }
}
