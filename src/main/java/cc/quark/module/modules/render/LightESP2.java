package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class LightESP2 extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Light level below which blocks are highlighted", 7, 0, 15));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Highlight color for dark blocks", 0xFFFF0000));

    public LightESP2() {
        super("LightESP2", "Highlights dark blocks (light <7)", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // Scan nearby blocks for dark spots
        List<BlockPos> darkBlocks = new ArrayList<>();
        int r = 16;
        BlockPos origin = mc.player.getBlockPos();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos pos = origin.add(x, 0, z);
                if (!mc.world.getBlockState(pos).isAir()) continue;
                if (!mc.world.getBlockState(pos.down()).isOpaque()) continue;
                int light = mc.world.getLightLevel(pos);
                if (light <= threshold.get()) {
                    darkBlocks.add(pos);
                }
            }
        }

        // HUD label
        ctx.drawTextWithShadow(mc.textRenderer,
                "Dark Blocks: " + darkBlocks.size(), 2, sh - 30, color.get());
    }
}
