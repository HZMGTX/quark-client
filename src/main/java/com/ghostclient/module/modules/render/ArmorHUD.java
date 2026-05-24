package com.ghostclient.module.modules.render;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventRender2D;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * ArmorHUD - renders armor item icons with durability bars on the 2D screen.
 *
 * Displays the four armor slots (helmet, chestplate, leggings, boots) and
 * the off-hand item. Each item shows its durability as a colored bar below it.
 * Positioned above the hotbar, left side of the screen.
 */
public class ArmorHUD extends Module {

    public ArmorHUD() {
        super("ArmorHUD", "Shows armor slots and durability on screen", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        ClientPlayerEntity player = mc.player;

        int screenH = mc.getWindow().getScaledHeight();
        int screenW = mc.getWindow().getScaledWidth();

        // Render armor from top (helmet) to bottom (boots) on the left side
        // Position: 6px from left, above hotbar area
        int x = 6;
        int startY = screenH - 56 - (4 * 18); // Above hotbar
        int slotSize = 18;

        // Armor slots: index 39=helmet, 38=chest, 37=legs, 36=boots
        int[] slots = {39, 38, 37, 36};

        for (int i = 0; i < 4; i++) {
            ItemStack stack = player.getInventory().getStack(slots[i]);
            int y = startY + i * slotSize;

            if (!stack.isEmpty()) {
                // Draw the item icon
                ctx.drawItem(stack, x, y);

                // Draw durability bar if item is damaged
                if (stack.isDamageable() && stack.getDamage() > 0) {
                    int maxDmg = stack.getMaxDamage();
                    int dmg = stack.getDamage();
                    float pct = (float)(maxDmg - dmg) / maxDmg;

                    int barWidth = (int)(13 * pct);
                    int barColor;
                    if (pct > 0.6f) barColor = 0xFF00FF44;
                    else if (pct > 0.3f) barColor = 0xFFFFFF00;
                    else barColor = 0xFFFF2222;

                    // Dark background bar
                    ctx.fill(x + 1, y + 13, x + 14, y + 14, 0xFF000000);
                    // Filled durability bar
                    ctx.fill(x + 1, y + 13, x + 1 + barWidth, y + 14, barColor);
                }

                // Durability text overlay
                if (stack.isDamageable()) {
                    int maxDmg = stack.getMaxDamage();
                    int dmg = stack.getDamage();
                    int remaining = maxDmg - dmg;
                    String label = remaining > 999 ? (remaining / 1000) + "k" : String.valueOf(remaining);
                    ctx.drawTextWithShadow(mc.textRenderer, label, x + 16, y + 4, 0xFFAAAAAA);
                }
            }
        }

        // Off-hand item
        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty()) {
            int y = startY + 4 * slotSize + 4;
            ctx.drawItem(offhand, x, y);
            if (offhand.isDamageable() && offhand.getDamage() > 0) {
                float pct = (float)(offhand.getMaxDamage() - offhand.getDamage()) / offhand.getMaxDamage();
                int barW = (int)(13 * pct);
                int barColor = pct > 0.6f ? 0xFF00FF44 : pct > 0.3f ? 0xFFFFFF00 : 0xFFFF2222;
                ctx.fill(x + 1, y + 13, x + 14, y + 14, 0xFF000000);
                ctx.fill(x + 1, y + 13, x + 1 + barW, y + 14, barColor);
            }
        }
    }
}
