package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmourStatus extends Module {

    public ArmourStatus() {
        super("ArmourStatus", "Shows armour durability bars on the HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int[] slots = {39, 38, 37, 36};
        String[] names = {"H", "C", "L", "B"};
        for (int i = 0; i < 4; i++) {
            ItemStack stack = mc.player.getInventory().getStack(slots[i]);
            int x = 5;
            int y = 5 + i * 10;
            if (stack.isEmpty() || !stack.isDamageable()) {
                ctx.drawTextWithShadow(mc.textRenderer, names[i] + ": -", x, y, 0xFFAAAAAA);
                continue;
            }
            int maxDmg = stack.getMaxDamage();
            int remaining = maxDmg - stack.getDamage();
            float pct = (float) remaining / maxDmg;
            int barColor = pct > 0.6f ? 0xFF00FF44 : pct > 0.3f ? 0xFFFFFF00 : 0xFFFF2222;
            ctx.fill(x + 10, y + 2, x + 10 + 40, y + 7, 0xFF333333);
            ctx.fill(x + 10, y + 2, x + 10 + (int)(40 * pct), y + 7, barColor);
            ctx.drawTextWithShadow(mc.textRenderer, names[i], x, y, barColor);
        }
    }
}
