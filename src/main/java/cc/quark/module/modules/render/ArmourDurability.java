package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmourDurability extends Module {

    private final IntSetting x       = register(new IntSetting("X", "HUD X position", 4, 0, 1920));
    private final IntSetting y       = register(new IntSetting("Y", "HUD Y position", 40, 0, 1080));
    private final BoolSetting color  = register(new BoolSetting("Color", "Color bars based on durability", true));

    // Armour slot indices: helmet=39, chest=38, legs=37, boots=36
    private static final int[] ARMOUR_SLOTS = {39, 38, 37, 36};
    private static final String[] ARMOUR_NAMES = {"Helmet", "Chest", "Legs", "Boots"};

    public ArmourDurability() {
        super("ArmourDurability", "Shows armor durability bars on HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        int px = x.get();
        int py = y.get();
        int barWidth = 50;
        int barHeight = 4;
        int spacing = 14;

        for (int i = 0; i < 4; i++) {
            ItemStack stack = mc.player.getInventory().getStack(ARMOUR_SLOTS[i]);
            int iy = py + i * spacing;

            // Label
            ctx.drawTextWithShadow(mc.textRenderer, ARMOUR_NAMES[i].substring(0, 1), px, iy, 0xFFAAAAAA);

            // Bar background
            ctx.fill(px + 8, iy + 2, px + 8 + barWidth, iy + 2 + barHeight, 0xFF333333);

            if (!stack.isEmpty() && stack.isDamageable()) {
                int maxDmg = stack.getMaxDamage();
                int dmg    = stack.getDamage();
                float pct  = maxDmg > 0 ? (float)(maxDmg - dmg) / maxDmg : 1f;
                int filled = (int)(barWidth * pct);

                int barColor;
                if (color.isEnabled()) {
                    if (pct > 0.6f)      barColor = 0xFF55FF55;
                    else if (pct > 0.3f) barColor = 0xFFFFAA00;
                    else                 barColor = 0xFFFF5555;
                } else {
                    barColor = 0xFFFFFFFF;
                }

                ctx.fill(px + 8, iy + 2, px + 8 + filled, iy + 2 + barHeight, barColor);

                // Durability number
                String numStr = (maxDmg - dmg) + "/" + maxDmg;
                ctx.drawTextWithShadow(mc.textRenderer, numStr, px + 8 + barWidth + 3, iy, 0xFFCCCCCC);
            } else if (stack.isEmpty()) {
                ctx.fill(px + 8, iy + 2, px + 8 + barWidth, iy + 2 + barHeight, 0xFF555555);
            }
        }
    }
}
