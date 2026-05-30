package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class InventoryViewer extends Module {

    private final IntSetting x = register(new IntSetting("X", "Panel X position", 50, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Panel Y position", 50, 0, 500));
    private final IntSetting scale = register(new IntSetting("Scale", "Item display scale factor", 1, 1, 3));

    public InventoryViewer() {
        super("InventoryViewer", "Draws a panel showing inventory contents as icons", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int px = x.get();
        int py = y.get();
        int sc = scale.get();
        int cellSize = 16 * sc;
        int cols = 9;

        // Draw background panel (slots 9-44: main inventory)
        int panelW = cols * cellSize + 2;
        int rows = 4; // 3 main rows + hotbar
        int panelH = rows * cellSize + 2;
        ctx.fill(px - 1, py - 1, px + panelW, py + panelH, 0x88000000);

        // Draw slots 9-44 (main inventory + hotbar)
        for (int slot = 9; slot <= 44; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            int index = slot - 9;
            int col = index % cols;
            int row = index / cols;

            int sx = px + col * cellSize;
            int sy = py + row * cellSize;

            if (!stack.isEmpty()) {
                // Draw item background based on type
                int bgColor = getItemColor(stack);
                ctx.fill(sx, sy, sx + cellSize, sy + cellSize, bgColor);

                // Draw the actual item icon
                ctx.drawItem(stack, sx + (cellSize - 16) / 2, sy + (cellSize - 16) / 2);
                if (stack.getCount() > 1) {
                    ctx.drawItemInSlot(mc.textRenderer, stack, sx + (cellSize - 16) / 2, sy + (cellSize - 16) / 2);
                }
            } else {
                ctx.fill(sx, sy, sx + cellSize, sy + cellSize, 0x44444444);
            }

            // Slot border
            ctx.drawBorder(sx, sy, cellSize, cellSize, 0x88888888);
        }
    }

    private int getItemColor(ItemStack stack) {
        if (stack.isEmpty()) return 0x44444444;
        String id = stack.getItem().toString().toLowerCase();
        if (id.contains("sword") || id.contains("axe") || id.contains("bow") || id.contains("crossbow")) {
            return 0x44FF4444; // Red for weapons
        }
        if (id.contains("helmet") || id.contains("chestplate") || id.contains("leggings") || id.contains("boots")) {
            return 0x444444FF; // Blue for armor
        }
        if (id.contains("food") || id.contains("bread") || id.contains("apple") || id.contains("meat") || id.contains("steak")) {
            return 0x4444FF44; // Green for food
        }
        if (id.contains("potion")) {
            return 0x44FF44FF; // Purple for potions
        }
        return 0x44888888; // Gray for everything else
    }
}
