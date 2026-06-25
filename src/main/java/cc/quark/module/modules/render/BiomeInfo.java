package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

public class BiomeInfo extends Module {

    private final BoolSetting showCoords = register(new BoolSetting("ShowCoords", "Also display XYZ coordinates below the biome name", true));
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 34, 0, 2160));

    public BiomeInfo() {
        super("BiomeInfo", "Displays the current biome name on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        RegistryEntry<Biome> biomeEntry = mc.world.getBiome(mc.player.getBlockPos());
        String biomeName = biomeEntry.getIdAsString();
        // Strip namespace (e.g. "minecraft:plains" -> "Plains")
        if (biomeName.contains(":")) biomeName = biomeName.split(":")[1];
        biomeName = biomeName.replace("_", " ");
        // Title-case
        String[] words = biomeName.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
            }
        }
        String display = "Biome: " + sb;

        int px = x.get(), py = y.get();
        ctx.drawTextWithShadow(mc.textRenderer, display, px, py, 0xFF55FFFF);

        if (showCoords.isEnabled()) {
            String coords = String.format("XYZ: %.0f / %.0f / %.0f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
            ctx.drawTextWithShadow(mc.textRenderer, coords, px, py + 10, 0xFFAAAAAA);
        }
    }
}
