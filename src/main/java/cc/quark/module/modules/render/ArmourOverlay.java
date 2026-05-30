package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public class ArmourOverlay extends Module {

    private final IntSetting posX = register(new IntSetting("X", "HUD X position", 4, 0, 500));
    private final IntSetting posY = register(new IntSetting("Y", "HUD Y position", 120, 0, 500));

    private static final int[] ARMOR_SLOTS = {39, 38, 37, 36}; // head, chest, legs, feet
    private static final String[] ARMOR_LABELS = {"Helm", "Chest", "Legs", "Boots"};

    public ArmourOverlay() {
        super("ArmourOverlay", "Shows durability bars for all 4 armor slots", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lineH = mc.textRenderer.fontHeight + 4;

        ctx.drawTextWithShadow(mc.textRenderer, "Armour:", x, y, 0xFFFFFFFF);
        y += mc.textRenderer.fontHeight + 2;

        for (int i = 0; i < 4; i++) {
            ItemStack stack = mc.player.getInventory().getStack(ARMOR_SLOTS[i]);
            String label = ARMOR_LABELS[i];

            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) {
                ctx.drawTextWithShadow(mc.textRenderer, label + ": --", x, y, 0xFF555555);
            } else {
                int maxDmg = stack.getMaxDamage();
                int remaining = maxDmg - stack.getDamage();
                float pct = maxDmg > 0 ? (float) remaining / maxDmg : 1f;
                int percent = (int)(pct * 100);
                int durColor = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;

                String text = label + ": " + percent + "%";
                ctx.drawTextWithShadow(mc.textRenderer, text, x, y, durColor);

                // Draw bar
                int barX = x;
                int barY = y + mc.textRenderer.fontHeight + 1;
                int barW = 60;
                int filledW = (int)(barW * pct);
                ctx.fill(barX, barY, barX + barW, barY + 2, 0xFF333333);
                ctx.fill(barX, barY, barX + filledW, barY + 2, durColor);
            }
            y += lineH + 3;
        }
    }
}
