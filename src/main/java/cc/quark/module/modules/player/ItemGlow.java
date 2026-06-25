package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ItemGlow extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Glow color for the held item highlight", 0xFF00AAFF));

    public ItemGlow() {
        super("ItemGlow", "Makes held item glow in the HUD", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        // State tracking tick — intentionally minimal
        if (mc.player == null) return;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int selectedSlot = mc.player.getInventory().selectedSlot;
        ItemStack held = mc.player.getInventory().getStack(selectedSlot);
        if (held.isEmpty()) return;

        int scaledWidth = mc.getWindow().getScaledWidth();
        int scaledHeight = mc.getWindow().getScaledHeight();

        // Hotbar slot position
        int hotbarX = scaledWidth / 2 - 91 + selectedSlot * 20;
        int hotbarY = scaledHeight - 22;

        int glowColor = color.get();
        // Draw a subtle glow outline around the selected slot
        ctx.fill(hotbarX - 1, hotbarY - 1, hotbarX + 17, hotbarY, glowColor);
        ctx.fill(hotbarX - 1, hotbarY + 16, hotbarX + 17, hotbarY + 17, glowColor);
        ctx.fill(hotbarX - 1, hotbarY, hotbarX, hotbarY + 16, glowColor);
        ctx.fill(hotbarX + 16, hotbarY, hotbarX + 17, hotbarY + 16, glowColor);
    }
}
