package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class HotbarHighlight extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Highlight color for selected hotbar slot", 0xFF00AAFF));

    private final IntSetting width = register(new IntSetting(
            "Width", "Border highlight width in pixels", 2, 1, 6));

    public HotbarHighlight() {
        super("HotbarHighlight", "Highlights selected hotbar slot with color", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // Hotbar slot dimensions (standard Minecraft HUD)
        int slotSize = 20;
        int hotbarWidth = slotSize * 9 + 2;
        int hotbarX = (sw - hotbarWidth) / 2;
        int hotbarY = sh - 22;

        int selectedSlot = mc.player.getInventory().selectedSlot;
        int slotX = hotbarX + selectedSlot * slotSize;

        int argb = color.get();
        int w = width.get();

        // Draw border around selected slot
        // Top
        ctx.fill(slotX - 1, hotbarY - 1, slotX + slotSize + 1, hotbarY - 1 + w, argb);
        // Bottom
        ctx.fill(slotX - 1, hotbarY + slotSize + 1 - w, slotX + slotSize + 1, hotbarY + slotSize + 1, argb);
        // Left
        ctx.fill(slotX - 1, hotbarY - 1, slotX - 1 + w, hotbarY + slotSize + 1, argb);
        // Right
        ctx.fill(slotX + slotSize + 1 - w, hotbarY - 1, slotX + slotSize + 1, hotbarY + slotSize + 1, argb);
    }
}
