package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Coordinates extends Module {

    private final IntSetting  posX       = register(new IntSetting("X", "Horizontal position", 4, 0, 500));
    private final IntSetting  posY       = register(new IntSetting("Y", "Vertical position",   4, 0, 500));
    private final BoolSetting showNether = register(new BoolSetting("Nether Equiv", "Show Nether/Overworld equivalent", true));
    private final BoolSetting showDir    = register(new BoolSetting("Direction",    "Show facing direction",             true));
    private final BoolSetting showBiome  = register(new BoolSetting("Biome",        "Show current biome",               false));

    public Coordinates() {
        super("Coordinates", "Displays XYZ coordinates and optional Nether equivalent on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
        int px = posX.get(), py = posY.get();

        ctx.drawTextWithShadow(mc.textRenderer,
                String.format("XYZ: %.1f / %.1f / %.1f", x, y, z), px, py, 0xFFFFFFFF);

        if (showNether.isEnabled()) {
            boolean inNether = mc.world.getRegistryKey() == World.NETHER;
            double nx = inNether ? x * 8 : x / 8;
            double nz = inNether ? z * 8 : z / 8;
            String label = inNether ? "OW: " : "NE: ";
            ctx.drawTextWithShadow(mc.textRenderer,
                    String.format(label + "%.1f / %.1f", nx, nz), px, py + 10, 0xFFAAAAAA);
        }

        if (showDir.isEnabled()) {
            Direction dir = mc.player.getHorizontalFacing();
            ctx.drawTextWithShadow(mc.textRenderer,
                    "Facing: " + dir.getName().toUpperCase(), px, py + 20, 0xFF88CCFF);
        }
    }
}
