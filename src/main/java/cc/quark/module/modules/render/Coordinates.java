package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class Coordinates extends Module {

    private final IntSetting   posX        = register(new IntSetting("X", "Horizontal position", 4, 0, 3000));
    private final IntSetting   posY        = register(new IntSetting("Y", "Vertical position", 4, 0, 3000));
    private final BoolSetting  showNether  = register(new BoolSetting("Nether Coords", "Show Nether/Overworld equivalent", true));
    private final BoolSetting  showChunk   = register(new BoolSetting("Chunk Info", "Show current chunk XZ", false));
    private final BoolSetting  showDir     = register(new BoolSetting("Direction", "Show facing direction", true));
    private final ColorSetting color       = register(new ColorSetting("Color", "Text color", 0xFFFFFFFF));

    public Coordinates() {
        super("Coordinates", "Enhanced XYZ display with nether/overworld conversion and chunk info", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;
        int col = color.get();

        ctx.drawTextWithShadow(mc.textRenderer,
                String.format("XYZ: %.1f / %.1f / %.1f", px, py, pz), x, y, col);

        if (showNether.isEnabled()) {
            boolean inNether = mc.world.getRegistryKey() == World.NETHER;
            double nx = inNether ? px * 8 : px / 8;
            double nz = inNether ? pz * 8 : pz / 8;
            String label = inNether ? "OW: " : "Nether: ";
            ctx.drawTextWithShadow(mc.textRenderer,
                    String.format(label + "%.1f / %.1f", nx, nz), x, y + lh, 0xFFAAAAAA);
        }

        if (showChunk.isEnabled()) {
            int dy = y + lh * (showNether.isEnabled() ? 2 : 1);
            ChunkPos chunk = mc.player.getChunkPos();
            ctx.drawTextWithShadow(mc.textRenderer,
                    "Chunk: " + chunk.x + ", " + chunk.z, x, dy, 0xFF88AACC);
        }

        if (showDir.isEnabled()) {
            int dy = y + lh * ((showNether.isEnabled() ? 1 : 0) + (showChunk.isEnabled() ? 1 : 0) + 1);
            String dirName = mc.player.getHorizontalFacing().getName().toUpperCase();
            ctx.drawTextWithShadow(mc.textRenderer,
                    "Facing: " + dirName, x, dy, 0xFF88CCFF);
        }
    }
}
